/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfcleanup;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.CharacterRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.RegexBasedLocationExtractionStrategy;
import com.itextpdf.kernel.utils.CompareTool;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import com.itextpdf.pdfcleanup.PdfCleanupProductInfo;
import com.itextpdf.kernel.Version;
import com.itextpdf.pdfcleanup.autosweep.CompositeCleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.ICleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.PdfAutoSweep;
import com.itextpdf.pdfcleanup.autosweep.RegexBasedCleanupStrategy;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.itextpdf.test.ITextTest.createOrClearDestinationFolder;

/**
 * @author Joris Schellekens
 */
@Category(IntegrationTest.class)
public class BigDocumentAutoCleanUpTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/BigDocumentAutoCleanUpTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/BigDocumentAutoCleanUpTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void redactLipsum() throws IOException, InterruptedException {

        String input = inputPath + "Lipsum.pdf";
        String output = outputPath + "redactLipsum.pdf";
        String cmp = inputPath + "cmp_redactLipsum.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, outputPath, "diff_redactLipsum_");
    }

    @Test
    public void redactTonySoprano() throws IOException, InterruptedException {

        String input = inputPath + "TheSopranos.pdf";
        String output = outputPath + "redactTonySoprano.pdf";
        String cmp = inputPath + "cmp_redactTonySoprano.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("Tony( |_)Soprano"));
        strategy.add(new RegexBasedCleanupStrategy("Soprano"));
        strategy.add(new RegexBasedCleanupStrategy("Sopranos"));

        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, outputPath, "diff_redactTonySoprano_");
    }

    @Test
    public void redactIPhoneUserManualMatchColor() throws IOException, InterruptedException {

        String input = inputPath + "iphone_user_guide_untagged.pdf";
        String output = outputPath + "redactIPhoneUserManualMatchColor.pdf";
        String cmp = inputPath + "cmp_redactIPhoneUserManualMatchColor.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new CustomLocationExtractionStrategy("(iphone)|(iPhone)"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, outputPath, "diff_redactIPhoneUserManualMatchColor_");
    }

    @Test
    public void redactIPhoneUserManual() throws IOException, InterruptedException {


        String input = inputPath + "iphone_user_guide_untagged.pdf";
        String output = outputPath + "redactIPhoneUserManual.pdf";
        String cmp = inputPath + "cmp_redactIPhoneUserManual.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(iphone)|(iPhone)"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfAutoSweep autoSweep = new PdfAutoSweep(strategy);
        autoSweep.cleanUp(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, outputPath, "diff_redactIPhoneUserManual_");
    }

    private void compareByContent(String cmp, String output, String targetDir, String diffPrefix) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareVisually(output, cmp, targetDir, diffPrefix + "_");

        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}

/*
 * color matching text redaction
 */

class CCharacterRenderInfo extends CharacterRenderInfo {

    private Color strokeColor;
    private Color fillColor;

    public CCharacterRenderInfo(TextRenderInfo tri) {
        super(tri);
        this.strokeColor = tri.getStrokeColor();
        this.fillColor = tri.getFillColor();
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public Color getFillColor() {
        return fillColor;
    }
}

class CustomLocationExtractionStrategy extends RegexBasedLocationExtractionStrategy implements ICleanupStrategy {

    private String regex;
    private Map<Rectangle, Color> colorByRectangle = new HashMap<>();

    public CustomLocationExtractionStrategy(String regex) {
        super(regex);
        this.regex = regex;
    }

    @Override
    public List<CharacterRenderInfo> toCRI(TextRenderInfo tri) {
        List<CharacterRenderInfo> cris = new ArrayList<>();
        for (TextRenderInfo subTri : tri.getCharacterRenderInfos()) {
            cris.add(new CCharacterRenderInfo(subTri));
        }
        return cris;
    }

    @Override
    public List<Rectangle> toRectangles(List<CharacterRenderInfo> cris) {
        Color col = ((CCharacterRenderInfo) cris.get(0)).getFillColor();
        List<Rectangle> rects = new ArrayList<>(super.toRectangles(cris));
        for (Rectangle rect : rects) {
            colorByRectangle.put(rect, col);
        }
        return rects;
    }

    @Override
    public Color getRedactionColor(IPdfTextLocation rect) {
        return colorByRectangle.containsKey(rect.getRectangle()) ? colorByRectangle.get(rect.getRectangle()) : ColorConstants.BLACK;
    }

    public ICleanupStrategy reset()
    {
        return new CustomLocationExtractionStrategy(regex);
    }
}
