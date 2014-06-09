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

import com.cognitionis.utils_basickit.FileUtils;
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
public class TimekTest {
    
    public TimekTest() {
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
     * Test of getNormTextandPattern method, of class TimexNormalizer.
     */
    @Test
    public void testGetNormTextandPattern()  throws Exception {
        System.out.println("getNormTextandPattern");
        Timek instance = new Timek();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.getClass().getResource("/time/timek-pattern-test.en-US.txt").toURI())), "UTF-8"))) {
                String line;
                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    linen++;
                    if(!line.trim().isEmpty() && line.matches("[^\\|]+\\|\\|.+")){
                        System.out.println("testing (line "+linen+"): "+line);
                        String [] test=line.trim().split("\\|\\|");
                        System.out.println("expected: "+test[1]);
                        System.out.println("predicted: "+instance.getNormTextandPattern(test[0]));
                        assertEquals(test[1], instance.getNormTextandPattern(test[0]));
                    }
                }
        }        
        
        if(FileUtils.URL_exists(this.getClass().getResource("/time/timek-pattern-test.es-ES.txt").toString())){
            System.out.println("getNormTextandPattern ES-ES");
            instance = new Timek(new Locale("es","ES"));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.getClass().getResource("/time/timek-pattern-test.es-ES.txt").toURI())), "UTF-8"))) {
                    String line;
                    int linen = 0;
                    while ((line = reader.readLine()) != null) {
                        linen++;
                        if(!line.trim().isEmpty() && line.matches("[^\\|]+\\|\\|.+")){
                            System.out.println("testing (line "+linen+"): "+line);
                            String [] test=line.trim().split("\\|\\|");
                            System.out.println("expected: "+test[1]);
                            String predicted=instance.getNormTextandPattern(test[0]);
                            System.out.println("predicted: "+predicted);
                            assertEquals(test[1], predicted);
                        }
                    }
            }        
            
        }        
    }
    
    
    
}