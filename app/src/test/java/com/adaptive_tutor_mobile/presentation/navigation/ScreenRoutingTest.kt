package com.adaptive_tutor_mobile.presentation.navigation

import com.adaptive_tutor_mobile.domain.model.auth.UserRole
import org.junit.Test
import kotlin.test.assertEquals

class ScreenRoutingTest {

    @Test
    fun `routeForRole maps each role to expected screen`() {
        assertEquals(Screen.AdminHome.route,    routeForRole(UserRole.ADMIN))
        assertEquals(Screen.OrgAdminHome.route, routeForRole(UserRole.ORGANIZATION_ADMIN))
        assertEquals(Screen.TeacherHome.route,  routeForRole(UserRole.TEACHER))
        assertEquals(Screen.StudentHome.route,  routeForRole(UserRole.STUDENT))
        assertEquals(Screen.ParentHome.route,   routeForRole(UserRole.PARENT))
    }

    @Test
    fun `routeForRole defaults UNKNOWN to Login`() {
        assertEquals(Screen.Login.route, routeForRole(UserRole.UNKNOWN))
    }
}