/*
 * Copyright 2013 hector.
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
package com.cognitionis.nlp_lang_models;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hector
 */
public class TextCategorizerTest {
    
    public TextCategorizerTest() {
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
     * Test of categorize method, of class TextCategorizer.
     */
    @Test
    public void testCategorize_String_int() {
        System.out.println("categorize");
        TextCategorizer instance = new TextCategorizer();
        assertEquals("en", instance.categorize("This is a test for English, should return that language. No problem with this, which is ok.", 25));
        assertEquals("es", instance.categorize("Esto es una prueba para el Español, no falla, es castellano. Además se nota.", 15));
    }

    /**
     * Test of categorize method, of class TextCategorizer.
     */
    @Test
    public void testCategorize_String() {
        System.out.println("categorize");
        TextCategorizer instance = new TextCategorizer();
        assertEquals("en", instance.categorize("This is a test for English, should return that language.  No problem with this, which is ok."));
        assertEquals("es", instance.categorize("Esto es una prueba para el Español, no falla, es castellano."));
    }
}