package com.adaptive_tutor_mobile.presentation.navigation

import com.adaptive_tutor_mobile.domain.model.auth.UserRole
import org.junit.Test
import kotlin.test.assertEquals

class ScreenRoutingTest {

    @Test
    fun `routeForRole keeps only student on mobile flow`() {
        assertEquals(Screen.RoleBlocked.route, routeForRole(UserRole.ADMIN))
        assertEquals(Screen.RoleBlocked.route, routeForRole(UserRole.ORGANIZATION_ADMIN))
        assertEquals(Screen.RoleBlocked.route, routeForRole(UserRole.TEACHER))
        assertEquals(Screen.StudentHome.route, routeForRole(UserRole.STUDENT))
        assertEquals(Screen.RoleBlocked.route, routeForRole(UserRole.PARENT))
    }

    @Test
    fun `routeForRole blocks unknown roles`() {
        assertEquals(Screen.RoleBlocked.route, routeForRole(UserRole.UNKNOWN))
    }
}
