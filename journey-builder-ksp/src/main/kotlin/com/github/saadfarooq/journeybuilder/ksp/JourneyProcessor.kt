package com.github.saadfarooq.journeybuilder.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class JourneyProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("com.github.saadfarooq.journeybuilder.Journey")
            .filterIsInstance<KSClassDeclaration>()

        val deferred = symbols.filter { !it.validate() }.toList()
        symbols.filter { it.validate() }.forEach { generate(it) }
        return deferred
    }

    private fun generate(formDecl: KSClassDeclaration) {
        val packageName = formDecl.packageName.asString()
        val formName = formDecl.simpleName.asString()
        val stateName = "${formName}State"

        val allSteps = formDecl.declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .toList()

        if (allSteps.isEmpty()) {
            logger.error("@Journey interface $formName has no nested step interfaces", formDecl)
            return
        }

        val stepNames = allSteps.map { it.simpleName.asString() }.toSet()
        val orderedSteps = buildChain(allSteps, stepNames)

        if (orderedSteps == null) {
            logger.error("@Journey interface $formName: could not resolve linear step chain", formDecl)
            return
        }

        val sealedClassName = ClassName(packageName, stateName)
        val journeyMachineClass = ClassName("com.github.saadfarooq.journeybuilder", "JourneyStateMachine")
        val backRestorableClass = ClassName("com.github.saadfarooq.journeybuilder", "BackRestorable")
        val backNavigableClass = ClassName("com.github.saadfarooq.journeybuilder", "BackNavigable")

        val fileBuilder = FileSpec.builder(packageName, stateName)

        val sealedClassBuilder = TypeSpec.classBuilder(stateName)
            .addModifiers(KModifier.SEALED)

        // Initial: data class with previous<Step1>: Step1? = null, implements BackRestorable
        val firstStepName = orderedSteps[0].simpleName.asString()
        val firstStepClass = ClassName(packageName, stateName, firstStepName)
        val initialPropName = "previous$firstStepName"
        val initialComingFromType = firstStepClass.copy(nullable = true)
        val initialBuilder = TypeSpec.classBuilder("Initial")
            .addModifiers(KModifier.DATA)
            .superclass(sealedClassName)
            .addSuperinterface(backRestorableClass.parameterizedBy(sealedClassName))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(initialPropName, initialComingFromType)
                            .defaultValue("null")
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder(initialPropName, initialComingFromType)
                    .initializer(initialPropName)
                    .build()
            )
            .addFunction(
                FunSpec.builder("withComingFrom")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("from", sealedClassName.copy(nullable = true))
                    .returns(sealedClassName)
                    .addStatement("return copy($initialPropName = from as? %T)", firstStepClass)
                    .build()
            )
        sealedClassBuilder.addType(initialBuilder.build())

        for ((index, step) in orderedSteps.withIndex()) {
            val stepName = step.simpleName.asString()
            val newProps = step.getAllProperties()
                .filter { it.parentDeclaration == step }
                .toList()
            val isLastStep = index == orderedSteps.size - 1

            val dataClassBuilder = TypeSpec.classBuilder(stepName)
                .addModifiers(KModifier.DATA)
                .superclass(sealedClassName)

            if (!isLastStep) {
                val nextStepName = orderedSteps[index + 1].simpleName.asString()
                val nextStepClass = ClassName(packageName, stateName, nextStepName)
                val propName = "previous$nextStepName"
                dataClassBuilder.addSuperinterface(backRestorableClass.parameterizedBy(sealedClassName))
                dataClassBuilder.addFunction(
                    FunSpec.builder("withComingFrom")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("from", sealedClassName.copy(nullable = true))
                        .returns(sealedClassName)
                        .addStatement("return copy($propName = from as? %T)", nextStepClass)
                        .build()
                )
            }

            val constructorBuilder = FunSpec.constructorBuilder()

            val prevStepClass = if (index == 0) {
                ClassName(packageName, stateName, "Initial")
            } else {
                ClassName(packageName, stateName, orderedSteps[index - 1].simpleName.asString())
            }
            constructorBuilder.addParameter(ParameterSpec.builder("prev", prevStepClass).build())
            dataClassBuilder.addProperty(
                PropertySpec.builder("prev", prevStepClass).initializer("prev").build()
            )
            dataClassBuilder.addSuperinterface(backNavigableClass.parameterizedBy(sealedClassName))
            dataClassBuilder.addFunction(
                FunSpec.builder("previousState")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(sealedClassName)
                    .addStatement("return prev")
                    .build()
            )

            for (prop in newProps) {
                val propName = prop.simpleName.asString()
                val propType = prop.type.toTypeName()
                constructorBuilder.addParameter(ParameterSpec.builder(propName, propType).build())
                dataClassBuilder.addProperty(
                    PropertySpec.builder(propName, propType).initializer(propName).build()
                )
            }

            if (!isLastStep) {
                val nextStepNameForProp = orderedSteps[index + 1].simpleName.asString()
                val nextStepClassForProp = ClassName(packageName, stateName, nextStepNameForProp)
                val propName = "previous$nextStepNameForProp"
                val previousType = nextStepClassForProp.copy(nullable = true)
                constructorBuilder.addParameter(
                    ParameterSpec.builder(propName, previousType)
                        .defaultValue("null")
                        .build()
                )
                dataClassBuilder.addProperty(
                    PropertySpec.builder(propName, previousType)
                        .initializer(propName)
                        .build()
                )
            }

            dataClassBuilder.primaryConstructor(constructorBuilder.build())
            sealedClassBuilder.addType(dataClassBuilder.build())
        }

        fileBuilder.addType(sealedClassBuilder.build())

        val initialClass = ClassName(packageName, stateName, "Initial")
        val firstStep = orderedSteps[0]
        val firstNewProps = firstStep.getAllProperties()
            .filter { it.parentDeclaration == firstStep }
            .toList()

        fileBuilder.addFunction(
            FunSpec.builder("next")
                .receiver(initialClass)
                .apply { firstNewProps.forEach { addParameter(it.simpleName.asString(), it.type.toTypeName()) } }
                .returns(ClassName(packageName, stateName, firstStep.simpleName.asString()))
                .addStatement(
                    "return %T(this, ${firstNewProps.joinToString(", ") { it.simpleName.asString() }})",
                    ClassName(packageName, stateName, firstStep.simpleName.asString())
                )
                .build()
        )

        for (index in 0 until orderedSteps.size - 1) {
            val step = orderedSteps[index]
            val nextStep = orderedSteps[index + 1]
            val stepClass = ClassName(packageName, stateName, step.simpleName.asString())
            val nextStepClass = ClassName(packageName, stateName, nextStep.simpleName.asString())
            val nextNewProps = nextStep.getAllProperties()
                .filter { it.parentDeclaration == nextStep }
                .toList()

            fileBuilder.addFunction(
                FunSpec.builder("next")
                    .receiver(stepClass)
                    .apply { nextNewProps.forEach { addParameter(it.simpleName.asString(), it.type.toTypeName()) } }
                    .returns(nextStepClass)
                    .addStatement(
                        "return %T(this, ${nextNewProps.joinToString(", ") { it.simpleName.asString() }})",
                        nextStepClass
                    )
                    .build()
            )
        }

        val machineType = journeyMachineClass.parameterizedBy(sealedClassName) as TypeName

        fileBuilder.addFunction(
            FunSpec.builder("nextFrom")
                .receiver(machineType)
                .addParameter("s", initialClass)
                .apply { firstNewProps.forEach { addParameter(it.simpleName.asString(), it.type.toTypeName()) } }
                .addStatement("next(s.next(${firstNewProps.joinToString(", ") { it.simpleName.asString() }}))")
                .build()
        )

        for (index in 0 until orderedSteps.size - 1) {
            val step = orderedSteps[index]
            val nextStep = orderedSteps[index + 1]
            val stepClass = ClassName(packageName, stateName, step.simpleName.asString())
            val nextNewProps = nextStep.getAllProperties()
                .filter { it.parentDeclaration == nextStep }
                .toList()

            fileBuilder.addFunction(
                FunSpec.builder("nextFrom")
                    .receiver(machineType)
                    .addParameter("s", stepClass)
                    .apply { nextNewProps.forEach { addParameter(it.simpleName.asString(), it.type.toTypeName()) } }
                    .addStatement("next(s.next(${nextNewProps.joinToString(", ") { it.simpleName.asString() }}))")
                    .build()
            )
        }

        fileBuilder.build().writeTo(codeGenerator, aggregating = false)
    }

    private fun buildChain(
        steps: List<KSClassDeclaration>,
        stepNames: Set<String>
    ): List<KSClassDeclaration>? {
        val root = steps.firstOrNull { step ->
            step.superTypes.none { it.resolve().declaration.simpleName.asString() in stepNames }
        } ?: return null

        val ordered = mutableListOf(root)
        val remaining = steps.toMutableList().also { it.remove(root) }

        while (remaining.isNotEmpty()) {
            val current = ordered.last()
            val next = remaining.firstOrNull { step ->
                step.superTypes.any {
                    it.resolve().declaration.simpleName.asString() == current.simpleName.asString()
                }
            } ?: return null
            ordered.add(next)
            remaining.remove(next)
        }

        return ordered
    }
}
