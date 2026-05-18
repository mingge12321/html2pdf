package tech.alei.html2pdf.util;

import com.github.jhonnymertz.wkhtmltopdf.wrapper.Pdf;
import com.github.jhonnymertz.wkhtmltopdf.wrapper.configurations.WrapperConfig;
import com.github.jhonnymertz.wkhtmltopdf.wrapper.params.Param;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class HtmlToPdfUtil
{
    public static void toPdfFile(String html, String fileName)
    {
        Pdf pdf = new Pdf();
        pdf.addPageFromString(html);

        try
        {
            File out = pdf.saveAs(fileName);
            trimTrailingBlankPages(out);
            addPageNumbers(out);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void toPdfFileWithHeader(String html, String fileName, String headerHtml)
    {
        File tempHeaderFile = null;
        try
        {
            // 创建临时页眉文件
            tempHeaderFile = File.createTempFile("header-", ".html");
            try (FileWriter writer = new FileWriter(tempHeaderFile))
            {
                writer.write(headerHtml);
            }

            Pdf pdf = new Pdf();
            pdf.addPageFromString(html);
            // 添加页眉配置
            pdf.addParam(new Param("--header-html", tempHeaderFile.getAbsolutePath()));
            // 设置页眉间距
            pdf.addParam(new Param("--header-spacing", "10"));

            File out = pdf.saveAs(fileName);
            trimTrailingBlankPages(out);
            addPageNumbers(out);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            // 删除临时文件
            if (tempHeaderFile != null && tempHeaderFile.exists())
            {
                tempHeaderFile.delete();
            }
        }
    }

    public static void toPdfFileWithHeaderAndFooter(String html, String fileName, String headerHtml, String footerHtml)
    {
        File tempHeaderFile = null;
        File tempFooterFile = null;
        try
        {
            // 创建临时页眉文件
            tempHeaderFile = File.createTempFile("header-", ".html");
            try (FileWriter writer = new FileWriter(tempHeaderFile))
            {
                writer.write(headerHtml);
            }

            // 创建临时页脚文件
            tempFooterFile = File.createTempFile("footer-", ".html");
            try (FileWriter writer = new FileWriter(tempFooterFile))
            {
                writer.write(footerHtml);
            }

            Pdf pdf = new Pdf();
            pdf.addPageFromString(html);
            // 添加页眉配置
            pdf.addParam(new Param("--header-html", tempHeaderFile.getAbsolutePath()));
            // 设置页眉间距
            pdf.addParam(new Param("--header-spacing", "10"));
            // 添加页脚配置
            pdf.addParam(new Param("--footer-html", tempFooterFile.getAbsolutePath()));
            // 设置页脚间距
            pdf.addParam(new Param("--footer-spacing", "10"));

            File out = pdf.saveAs(fileName);
            trimTrailingBlankPages(out);
            addPageNumbers(out);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            // 删除临时文件
            if (tempHeaderFile != null && tempHeaderFile.exists())
            {
                tempHeaderFile.delete();
            }
            if (tempFooterFile != null && tempFooterFile.exists())
            {
                tempFooterFile.delete();
            }
        }
    }

    public static void toPdfResponse(String html, HttpServletResponse response)
    {
        Pdf pdf = new Pdf();
        pdf.addPageFromString(html);
        response.setContentType("application/pdf");
        try
        {
            byte[] bytes = pdf.getPDF();
            bytes = trimTrailingBlankPages(bytes);
            OutputStream out = response.getOutputStream();
            out.write(bytes);
            out.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 移除 wkhtmltopdf 在文档末尾产生的无文字空白页。
     */
    static void trimTrailingBlankPages(File pdfFile) throws IOException
    {
        if (pdfFile == null || !pdfFile.exists())
        {
            return;
        }
        byte[] trimmed = trimTrailingBlankPages(Files.readAllBytes(pdfFile.toPath()));
        Files.write(pdfFile.toPath(), trimmed);
    }

    static byte[] trimTrailingBlankPages(byte[] pdfBytes) throws IOException
    {
        if (pdfBytes == null || pdfBytes.length == 0)
        {
            return pdfBytes;
        }
        try (PDDocument document = PDDocument.load(pdfBytes))
        {
            while (document.getNumberOfPages() > 1)
            {
                int lastIndex = document.getNumberOfPages() - 1;
                String text = extractPageText(document, lastIndex + 1);
                if (!isBlankPageText(text))
                {
                    break;
                }
                document.removePage(lastIndex);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private static String extractPageText(PDDocument document, int pageNumber) throws IOException
    {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageNumber);
        stripper.setEndPage(pageNumber);
        return stripper.getText(document);
    }

    private static boolean isBlankPageText(String text)
    {
        if (text == null)
        {
            return true;
        }
        return text.replaceAll("\\s+", "").length() == 0;
    }

    static void addPageNumbers(File pdfFile) throws IOException
    {
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        byte[] numbered = addPageNumbers(pdfBytes);
        Files.write(pdfFile.toPath(), numbered);
    }
    
    static byte[] addPageNumbers(byte[] pdfBytes) throws IOException
    {
        if (pdfBytes == null || pdfBytes.length == 0)
        {
            return pdfBytes;
        }
        try (PDDocument document = PDDocument.load(pdfBytes))
        {
            int totalPages = document.getNumberOfPages();
            if (totalPages <= 1)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.save(out);
                return out.toByteArray();
            }
            
            float pageWidth = 595f;
            
            // 从第2页开始添加页码（索引为1），页码从1开始计数
            for (int pageIndex = 1; pageIndex < totalPages; pageIndex++)
            {
                PDPage page = document.getPage(pageIndex);
                int pageNumber = pageIndex;
                
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                        PDPageContentStream.AppendMode.PREPEND, true, true))
                {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.setLeading(12f);
                    
                    String pageText = String.valueOf(pageNumber);
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(pageText) / 1000 * 10;
                    float xPosition = (pageWidth - textWidth) / 2;
                    float yPosition = 15;
                    contentStream.newLineAtOffset(xPosition, yPosition);
                    contentStream.showText(pageText);
                    contentStream.endText();
                }
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
