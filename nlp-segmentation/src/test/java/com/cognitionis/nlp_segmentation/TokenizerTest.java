/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cognitionis.nlp_segmentation;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
//import org.apache.commons.io.FileUtils;

/**
 *
 * @author hector
 */
public class TokenizerTest {

    public TokenizerTest() {
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
     * Test of tokenize method, of class Tokenizer_PTB_Rulebased.
     */
    @Test
    public void testTokenize_File() throws Exception {
        System.out.println("tokenize");
        //java.net.URL url = this.class.getResource("test/resources/tokenizer/test-input.txt");
        //File f=FileUtils.toFile(this.getClass().getResource("/tokenizer/test-input.txt.tokenized"));
        File f_in = new File(this.getClass().getResource("/tokenizer/test-input.txt").toURI());
        File f_out = new File(this.getClass().getResource("/tokenizer/test-input.txt.tokenized").toURI());
        String expResultString = new String(java.nio.file.Files.readAllBytes(f_out.toPath()),"UTF-8");
        String inputString = new String(java.nio.file.Files.readAllBytes(f_in.toPath()),"UTF-8");
        Tokenizer_PTB_Rulebased instance = new Tokenizer_PTB_Rulebased(false); // tokenize without sentence splitting
        String result = instance.tokenize(inputString);
        assertEquals(expResultString, result);
        //assertArrayEquals
    }
}