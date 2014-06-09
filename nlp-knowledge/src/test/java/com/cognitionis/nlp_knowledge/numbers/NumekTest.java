/*
 * Copyright 2013 Hector_Llorens.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognitionis.nlp_knowledge.numbers;

import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hector_Llorens
 */
public class NumekTest {
    
    private Numek instance;
    
    public NumekTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
                
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {

    }

    /**
     * Test of calc_and_sum_frac method, of class Numek.
     */
    @Test
    public void testCalc_and_sum_frac() {
        System.out.println("calc_and_sum_frac");
        assertEquals((Double) 1.5,(Double) Numek.calc_and_sum_frac("1 1/2"));
    }

    /**
     * Test of text2number method, of class Numek.
     */
    @Test
    public void testText2number() {
        System.out.println("text2number");
        instance = new Numek(new Locale("en","US"));
        assertEquals("25", instance.text2number("twenty five"));
        assertEquals("0.01", instance.text2number("00.010"));
        assertEquals("2000025", instance.text2number("two million twenty five"));
        assertEquals("2000025", instance.text2number("two million and twenty five"));
        instance=null;
        instance = new Numek(new Locale("es","ES"));
        assertEquals("23002105", instance.text2number("VEINTITRÉS MILLONES DOS MIL CIENTO CINCO"));
        assertEquals("24", instance.text2number("DoS doceNas"));
        instance=null;                
    }

    /**
     * Test of countOccurrencesOf method, of class Numek.
     */
    @Test
    public void testCountOccurrencesOf() {
        System.out.println("countOccurrencesOf");
        assertEquals(3, Numek.countOccurrencesOf("aaa", 'a'));
        assertEquals(0, Numek.countOccurrencesOf("aaa", 'b'));
        assertEquals(2, Numek.countOccurrencesOf("aba", 'a'));
    }

    /**
     * Test of Roman2Decimal method, of class Numek.
     */
    @Test
    public void testRoman2Decimal() {
        System.out.println("Roman2Decimal");
        assertEquals("1", Numek.Roman2Decimal("i"));
        assertEquals(null, Numek.Roman2Decimal("VL"));
        assertEquals("45", Numek.Roman2Decimal("XLV"));
        assertEquals(null, Numek.Roman2Decimal("IIII"));
        assertEquals("4", Numek.Roman2Decimal("iv"));
        assertEquals(null, Numek.Roman2Decimal("VIV"));
        assertEquals("9", Numek.Roman2Decimal("IX"));
        assertEquals(null, Numek.Roman2Decimal("CMM"));
        assertEquals("1900", Numek.Roman2Decimal("MCM"));
        assertEquals(null, Numek.Roman2Decimal("IXVI"));
        assertEquals("15", Numek.Roman2Decimal("XV"));
        assertEquals(null, Numek.Roman2Decimal("IVI"));
        assertEquals("5", Numek.Roman2Decimal("v"));
        assertEquals(null, Numek.Roman2Decimal("XXL"));
        assertEquals("30", Numek.Roman2Decimal("XXX"));
        assertEquals(null, Numek.Roman2Decimal("IC"));
        assertEquals("99", Numek.Roman2Decimal("XCIX"));
        assertEquals(null, Numek.Roman2Decimal("XIL"));
        assertEquals("41", Numek.Roman2Decimal("XLI"));
        assertEquals(null, Numek.Roman2Decimal("IXL"));
        assertEquals("39", Numek.Roman2Decimal("XXXIX"));
    }
}