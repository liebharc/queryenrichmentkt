package com.github.liebharc.queryenrichment

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class InMemoryPlanBuilderTest {

    @Before
    fun resetInMemoryDb() {
        InMemoryQueryBuilder.database.clear()
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidSelectorsTest() {
        val steps =listOf(
                InMemoryQueryBuilder.studentId,
                InMemoryQueryBuilder.studentId)
        InMemoryPlanBuilder(steps)
    }

    @Test
    fun multipleDomainsTest() {
        val steps = listOf(
                InMemoryQueryBuilder.studentId,
                SelectorBuilder(Attributes.teacherId).addColumn("ID").build())
        val planBuilder = InMemoryPlanBuilder(steps)
        val plan = planBuilder.build(Request(steps.map { it.attribute}.toList()))
        Assert.assertNotNull(plan)
    }

    @Test
    fun findSelectorsSimpleTest() {
        val steps = Arrays.asList(
                InMemoryQueryBuilder.studentId,
                InMemoryQueryBuilder.firstName,
                InMemoryQueryBuilder.lastName,
                SelectorBuilder(Attributes.teacherId).addColumn("ID").build(),
                SelectorBuilder(Attributes.teacherFirstName).addColumn("FIRST_NAME").build(),
                SelectorBuilder(Attributes.teacherLastName).addColumn("LAST_NAME").build())

        val planBuilder = InMemoryPlanBuilder(steps)

        val plan = planBuilder.build(
                Request(
                        Arrays.asList(Attributes.studentId,
                                Attributes.lastName,
                                Attributes.firstName)))

        Assert.assertArrayEquals(plan.steps.toTypedArray(), arrayOf(steps[0], steps[2], steps[1]))
    }

    @Test
    fun optionalDependenciesTest() {
        InMemoryQueryBuilder.database.add(Student(10, "David", "Tenant"))
        val steps = listOf(
                InMemoryQueryBuilder.studentId,
                InMemoryQueryBuilder.firstName,
                InMemoryQueryBuilder.lastName,
                InMemoryQueryBuilder.fullName)

        val planBuilder = InMemoryPlanBuilder(steps)

        val request = Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.fullName))
        val plan = planBuilder.build(
                request)
        val result = plan.execute(request, null)
        val firstRow = result.results[0]
        Assert.assertEquals("Tenant", firstRow[1])
        Assert.assertEquals("Tenant", firstRow[2])
    }

    @Test
    fun optionalDependenciesWithAllInputsTest() {
        InMemoryQueryBuilder.database.add(Student(10, "David", "Tenant"))
        val steps = listOf(
                InMemoryQueryBuilder.studentId,
                InMemoryQueryBuilder.firstName,
                InMemoryQueryBuilder.lastName,
                InMemoryQueryBuilder.fullName)

        val planBuilder = InMemoryPlanBuilder(steps)

        val request = Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.firstName,
                        Attributes.lastName,
                        Attributes.fullName))
        val plan = planBuilder.build(
                request)
        val result = plan.execute(request, null)
        val firstRow = result.results[0]
        Assert.assertEquals("David", firstRow[1])
        Assert.assertEquals("Tenant", firstRow[2])
        Assert.assertEquals("David Tenant", firstRow[3])
    }

    @Test
    fun planCacheTest() {
        val steps = this.createDefaultSteps()

        val planBuilder = InMemoryPlanBuilder(steps)
        val planCache = PlanCache(10, planBuilder)
        val request = Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName))

        val plan1 = planCache.getOrBuildPlan(request)
        val plan2 = planCache.getOrBuildPlan(request)
        Assert.assertSame(plan1, plan2)
    }

    @Test
    fun javaFiltersTest() {
        InMemoryQueryBuilder.database.add(Student(10, "David", "Tenant"))
        InMemoryQueryBuilder.database.add(Student(11, "Matt", "Smith"))

        val steps = this.createDefaultSteps()
        val planBuilder = InMemoryPlanBuilder(steps)
        val request = Request(
                Arrays.asList<Attribute<*>>(Attributes.studentId),
                Arrays.asList(SimpleExpression.eq(Attributes.lastName, "Smith")))
        val build = planBuilder.build(request)
        val result = build.execute(request, null)
        Assert.assertEquals(1, result.results.size.toLong())
    }

    @Test
    fun referenceTest() {
        val student = Student(10, "David", "Tenant")
        InMemoryQueryBuilder.database.add(student)
        val steps = listOf(InMemoryQueryBuilder.reference)

        val planBuilder = InMemoryPlanBuilder(steps)

        val request = Request(Arrays.asList(Attributes.reference))
        val plan = planBuilder.build(request)
        val result = plan.execute(request, null)
        val firstRow = result.results[0]
        Assert.assertEquals(student, firstRow[0])
    }

    @Test
    fun selectorFromReference() {
        val student = Student(10, "David", "Tenant")
        InMemoryQueryBuilder.database.add(student)
        val steps = listOf(
                InMemoryQueryBuilder.reference,
                InMemoryQueryBuilder.firstName)

        val planBuilder = InMemoryPlanBuilder(steps)

        val request = Request(Arrays.asList(Attributes.reference, Attributes.firstName))
        val plan = planBuilder.build(request)
        val result = plan.execute(request, null)
        val firstRow = result.results[0]
        Assert.assertEquals(student, firstRow[0])
        Assert.assertEquals(student.firstName, firstRow[1])
    }

    private fun createDefaultSteps(): List<ExecutableStep<*, Any?>> {
        return listOf(
                InMemoryQueryBuilder.studentId,
                InMemoryQueryBuilder.firstName,
                InMemoryQueryBuilder.lastName,
                InMemoryQueryBuilder.fullName)
    }
}