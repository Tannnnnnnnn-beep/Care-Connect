package com.example.careconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.careconnect.models.Donor;
import com.example.careconnect.models.Hospital;
import com.example.careconnect.models.User;
import com.example.careconnect.utils.Constants;
import com.example.careconnect.utils.ValidationUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class CareConnectUnitTest {

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtils.isValidEmail("donor@careconnect.org"));
        assertTrue(ValidationUtils.isValidEmail("emergency.help@hospital.co.in"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail("user@"));
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
    }

    @Test
    public void testPhoneValidation() {
        assertTrue(ValidationUtils.isValidPhone("9876543210"));
        assertTrue(ValidationUtils.isValidPhone("+91 9876543210"));
        assertTrue(ValidationUtils.isValidPhone("+91-98765-43210"));
        assertFalse(ValidationUtils.isValidPhone("12345"));
        assertFalse(ValidationUtils.isValidPhone(null));
        assertFalse(ValidationUtils.isValidPhone(""));
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(ValidationUtils.isValidPassword("password123"));
        assertTrue(ValidationUtils.isValidPassword("secret"));
        assertFalse(ValidationUtils.isValidPassword("12345"));
        assertFalse(ValidationUtils.isValidPassword(""));
        assertFalse(ValidationUtils.isValidPassword(null));
    }

    @Test
    public void testUserModel() {
        User user = new User("u123", "Amit Kumar", "amit@example.com", "9876543210", "B+", "Delhi", true, 1000L);
        assertEquals("u123", user.getUid());
        assertEquals("Amit Kumar", user.getName());
        assertEquals("amit@example.com", user.getEmail());
        assertEquals("9876543210", user.getPhone());
        assertEquals("B+", user.getBloodGroup());
        assertEquals("Delhi", user.getCity());
        assertTrue(user.isDonor());
    }

    @Test
    public void testDonorModel() {
        Donor donor = new Donor("d123", "u123", "Priya Sharma", "O+", "+91 9845012345", "Bengaluru", true, 2000L);
        assertEquals("d123", donor.getDonorId());
        assertEquals("Priya Sharma", donor.getName());
        assertEquals("O+", donor.getBloodGroup());
        assertTrue(donor.isAvailable());

        donor.setAvailable(false);
        assertFalse(donor.isAvailable());
    }

    @Test
    public void testHospitalModelAndBeds() {
        Hospital hospital = new Hospital("h1", "City Hospital", "MG Road", "Mumbai", "+91 9820011223", 20, 5, 2, true, 3000L);
        assertEquals("h1", hospital.getHospitalId());
        assertEquals("City Hospital", hospital.getName());
        assertEquals(20, hospital.getGeneralBeds());
        assertEquals(5, hospital.getIcuBeds());
        assertEquals(2, hospital.getEmergencyBeds());
        assertEquals("+91 9820011223", hospital.getContactPhone());
        assertTrue(hospital.isActive());
    }

    @Test
    public void testConstants() {
        assertNotNull(Constants.BLOOD_GROUPS);
        assertEquals(8, Constants.BLOOD_GROUPS.length);
        assertNotNull(Constants.BLOOD_GROUPS_FILTER);
        assertEquals(9, Constants.BLOOD_GROUPS_FILTER.length);
        assertEquals("All", Constants.BLOOD_GROUPS_FILTER[0]);
    }
}
