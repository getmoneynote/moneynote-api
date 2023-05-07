package cn.biq.mn.user.book;

import cn.biq.mn.user.balanceflow.BalanceFlowDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.base.BaseController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;
import cn.biq.mn.base.response.DataResponse;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController extends BaseController {

    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(BookQueryForm form, Pageable page) {
        return new PageResponse<>(bookService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public BaseResponse handleGet(@PathVariable("id") Integer id) {
        return new DataResponse<>(bookService.get(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody BookAddForm form) {
        return new BaseResponse(bookService.add(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll(BookQueryForm form) {
        return new DataResponse<>(bookService.queryAll(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody BookUpdateForm form) {
        return new BaseResponse(bookService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(bookService.remove(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggle")
    public BaseResponse handleToggle(@PathVariable("id") Integer id) {
        return new BaseResponse(bookService.toggle(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/template")
    public BaseResponse handleAddByTemplate(@Valid @RequestBody BookAddByTemplateForm form) {
        return new BaseResponse(bookService.addByTemplate(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/export")
    public void handleExport(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
        // 创建一个新的工作簿
        Workbook workbook = new SXSSFWorkbook();
        // 创建一个新的工作表
        Sheet sheet = workbook.createSheet("Data");
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "标题", "交易类型", "金额", "时间", "账户", "分类",
            "标签", "交易对象", "备注", "是否确认", "是否统计"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 写入数据
        List<BalanceFlowDetails> balanceFlowDetailsList = bookService.exportFlow(id);
        int rowNum = 1;
        for (BalanceFlowDetails item : balanceFlowDetailsList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getTitle());
            row.createCell(1).setCellValue(item.getTypeName());
            row.createCell(2).setCellValue(item.getAmount().toString());

            CellStyle cellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm"));
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            Cell cell = row.createCell(3);
            cell.setCellValue(new Date(item.getCreateTime()));
            cell.setCellStyle(cellStyle);

            row.createCell(4).setCellValue(item.getAccountName());
            row.createCell(5).setCellValue(item.getCategoryName());
            row.createCell(6).setCellValue(item.getTagsName());

            if (item.getPayee() != null) {
                row.createCell(7).setCellValue(item.getPayee().getName());
            } else {
                row.createCell(7).setCellValue("");
            }

            row.createCell(8).setCellValue(item.getNotes());
            row.createCell(9).setCellValue(item.getConfirm());
            if (item.getInclude() != null) {
                row.createCell(10).setCellValue(item.getInclude());
            }

        }
        sheet.setColumnWidth(3, 19*256);

        // 设置 HTTP 响应头
        response.setContentType("application/vnd.ms-excel");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        response.setHeader("Content-disposition", "attachment; filename=users_" + currentDateTime + ".xlsx");

        // 将工作簿写入响应流
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
