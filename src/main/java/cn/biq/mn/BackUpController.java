package cn.biq.mn;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Statement;

@RestController
@RequiredArgsConstructor
public class BackUpController {

    private final DataSource dataSource;

    @GetMapping("/backup")
    public void backup(HttpServletResponse response) throws Exception {
        String fileName = backupSql();

        File file = new File("./data/" + fileName);

        response.setHeader("Content-Disposition",
                "attachment; filename=" + fileName);

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {

            fis.transferTo(os);
        }
    }

    public String backupSql() throws Exception {

        String dirPath = "./data";
        new File(dirPath).mkdirs();

        String fileName = "data" + ".sql";

        String filePath = dirPath + "/" + fileName;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SCRIPT TO '" + filePath + "'");
        }

        return fileName;
    }

}
