package com.upload.demo.controller;

import com.upload.demo.util.FileLoadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class FileLoad {
    @RequestMapping(value = "/test")
    @ResponseBody
    public String myTest(){
        return "yes";
    }

    /**
     *
     * @param file
     * @param request
     * @param session
     * @return
     */
    @RequestMapping("/up")
    @ResponseBody
    public String up(@RequestParam("file")MultipartFile file, HttpServletRequest request, HttpSession session){

        if (file.isEmpty()) {
            // 设置错误状态码

            return "isEmpty";
        }
         // 拿到文件名
        String filename = file.getOriginalFilename();
        // 存放上传图片的文件夹
        File fileDir = FileLoadUtil.getImgDirFile();
        // 输出文件夹绝对路径
        System.out.println(fileDir.getAbsolutePath());
        try {
            // 构建真实的文件路径
            File newFile = new File(fileDir.getAbsolutePath() + File.separator + filename);

            System.out.println(newFile.getAbsolutePath());
            // 上传图片到 -》 “绝对路径”
            file.transferTo(newFile);
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "faild";

    }

    /**
     * 下载
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/down")
    @ResponseBody
    public  static String downloadFile(HttpServletRequest request, HttpServletResponse response,String fileName) {
        System.out.println("down");
        if (fileName != null) {
            //设置文件路径
            File fileDir=FileLoadUtil.getImgDirFile();
            File file = new File(fileDir.getAbsolutePath()+"/"+fileName);
            //File file = new File(realPath , fileName);
            if (file.exists()) {
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);// 设置文件名
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    return "下载成功";
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "下载失败";
    }
    /**
     * 批量下载
     * 生成zip
     */
    @RequestMapping("many")
    @ResponseBody
    public static void batchDownLoadFile(HttpServletRequest request,HttpServletResponse response){
        String[] filepath=new String[]{FileLoadUtil.getImgDirFile().getAbsolutePath()+File.separator+"1.jpg",FileLoadUtil.getImgDirFile().getAbsolutePath()+File.separator+"2.png",FileLoadUtil.getImgDirFile().getAbsolutePath()+File.separator+"5.jpg"};
        String loginName="login";
        //输出到zip的文件名
        String[] documentName=new String[]{"1.jpg","2.png","3.jpg"};
        byte[] buffer = new byte[1024];
        Date date=new Date();
        //生成文件名
        String str=loginName+date.getTime()+".zip";
        //生成zip文件存放位置
        String strZipPath = FileLoadUtil.getImgDirFile().getAbsolutePath()+File.separator+loginName+date.getTime()+".zip";
        File file=new File(FileLoadUtil.getImgDirFile().getAbsolutePath());
        if(!file.isDirectory() && !file.exists()){

            // 创建多层目录
            file.mkdirs();
        }
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(strZipPath));
            // 需要同时下载的多个文件
            for (int i = 0; i < filepath.length; i++) {
                File f=new File(filepath[i]);
                FileInputStream fis = new FileInputStream(f);
                System.out.println(documentName[i]);
                out.putNextEntry(new ZipEntry(documentName[i]));
                int len;
                // 读入需要下载的文件的内容，打包到zip文件
                while ((len = fis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                fis.close();
            }
            out.close();
            System.out.println("zip:"+str);
            FileLoad.downloadFile(request, response, str);
            File temp=new File(strZipPath);
            if(temp.exists()){
                System.out.println("del操作");
                temp.delete();
            }
        } catch (Exception e) {
            System.out.println("文件下载错误");
        }
    }




}
