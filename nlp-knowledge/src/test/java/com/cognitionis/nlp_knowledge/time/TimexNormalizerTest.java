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
package com.cognitionis.nlp_knowledge.time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
public class TimexNormalizerTest {
    
    public TimexNormalizerTest() {
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
     * Test of getNormalize method, of class TimexNormalizer.
     */
    @Test
    public void testNormalize() throws Exception {
        System.out.println("normalize");
        TimexNormalizer instance = new TimexNormalizer();
        //read and test file for English
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.getClass().getResource("/time/timex-test.en-US.txt").toURI())), "UTF-8"))) {
            String line;
                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    linen++;
                    if(!line.trim().isEmpty() && line.matches("[^\\|]+\\|[^\\|]+")){
                        System.out.println("testing (line "+linen+"): "+line);
                        String [] test=line.trim().split("\\|");
                        System.out.println("expected: "+test[1]);
                        String predicted=instance.normalize(test[0]);
                        System.out.println("predicted: "+predicted);
                        assertEquals(test[1], predicted);
                    }
                }
        }
        TimexNormalizer instanceES = new TimexNormalizer(new Locale("es","ES"));
        //same for spanish
    }    
    
    
}