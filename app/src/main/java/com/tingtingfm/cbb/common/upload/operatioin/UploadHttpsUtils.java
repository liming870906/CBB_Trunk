package com.tingtingfm.cbb.common.upload.operatioin;

import com.alibaba.fastjson.JSON;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.bean.UploadFirstResponse;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.upload.DBOperationUtils;
import com.tingtingfm.cbb.common.upload.ParamsHelper;
import com.tingtingfm.cbb.common.upload.config.UploadConfiguration;
import com.tingtingfm.cbb.common.upload.response.UploadChunksStartResponse;
import com.tingtingfm.cbb.common.utils.BaseUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lqsir on 2017/4/18.
 */

public class UploadHttpsUtils {
    private static HttpsURLConnection getHttpsUrlConnection(String uploadUrl, String type, UploadConfiguration configuration) throws Exception {
        final int mimeType = ParamsHelper.getUploadType(type);

        URL url = new URL(uploadUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(configuration.getSSLSocketFactory());
        conn.setHostnameVerifier(configuration.getHostnameVerifier());
        conn.setChunkedStreamingMode(500 * 1024);

        if (mimeType == 1) {
            conn.setReadTimeout(30 * 1000);
            conn.setConnectTimeout(30 * 1000);
        } else {
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60 * 1000);
        }
        conn.setDoInput(true); // 设置为可读取，用于下载
        conn.setDoOutput(true); // 设置为可写入，用于上传
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST"); // 请求方式

        setHttpsRequestProperty(conn, configuration);

        return conn;
    }

    private static void setHttpsRequestProperty(HttpsURLConnection conn, UploadConfiguration configuration) {
        conn.setRequestProperty("Charset", "utf-8"); // 设置编码
        conn.setRequestProperty("connection", "keep-alive");
        //conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
        conn.setRequestProperty("Content-Type", configuration.getContent_type() + ";boundary=" + configuration.getBoundary());
    }

    /**
     * 上传音频信息，从服务端返回结果信息.
     * @param info 上传素材信息
     * @param configuration 上传公共配置信息
     * @return 将服务端返回内容以字符串形式返回，如何出现异常，返回内容为空字符串
     */
    public static String getUploadAudioInfo(MediaInfo info, UploadConfiguration configuration) {
        Map<String, String> params = ParamsHelper.getAudioParams(info);
        HttpsURLConnection conn = null;
        DataOutputStream dos = null;
        String body = "";

        try {
            conn = getHttpsUrlConnection(UrlManager.UPLOAD_AUDIO_INFO, info.getMime_type(), configuration);
            dos = new DataOutputStream(conn.getOutputStream());
            UploadWriteUtils.writeParams(params, dos, configuration);

            int res = conn.getResponseCode();
            if (res == 200) {
                body = BaseUtils.inputStreamToString(conn.getInputStream());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeIo(conn, dos);
        }

        return body;
    }

    public static String getSliceUploadInfo(final MediaInfo info, UploadConfiguration configuration) {
        String body = "";

        try {
            //第一步，传递一些必须参数给服务器作较验
            UploadFirstResponse response = uploadFirstStep(info, configuration);

            //第二步，正在上传文件
            int identity = uploadSecondStep(response, info, configuration);

            //第三步，上传文件完毕，提交服务器进行对比
            body = uploadThreeStep(identity, info, configuration);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }

        return body;
    }

    private static UploadFirstResponse uploadFirstStep(MediaInfo info, UploadConfiguration configuration) throws Exception {
        // 检查当前文件是否成功执行第一步上传
        if (info.getSliceId() != 0) {
            UploadFirstResponse result = DBOperationUtils.getUploadInfoForMediaInfo(info);

            if (result != null && result.identity > 0) {
                // 从数据库获取上传Id，切片总数
                System.out.println("uploadFirstStep ----------- uploadStatus = " + result.toString());
                if (info.getDefaultSliceCount() == result.count) {
                    info.setSliceId(result.identity);
                    info.setSliceCount(result.count);
                    info.setSuccessIds(result.successSlice);
                    return result;
                }
            }
        }

        HttpsURLConnection conn = null;
        DataOutputStream dos = null;

        int count = 0;
        int identity = -1;

        try {
            conn = getHttpsUrlConnection(UrlManager.UPLOAD_SLICE_START, info.getMime_type(), configuration);
            dos = new DataOutputStream(conn.getOutputStream());
            Map<String, String> params = ParamsHelper.getStartUploadParams(info);
            count = Integer.parseInt(params.get("split_counts"));
            UploadWriteUtils.writeParams(params, dos, configuration);

            int resCode = conn.getResponseCode();

            if (resCode == 200) {
                String content = BaseUtils.inputStreamToString(conn.getInputStream());
                UploadChunksStartResponse response = JSON.parseObject(content, UploadChunksStartResponse.class);

                if (response != null && response.getData() != null) {
                    identity = response.getData().getIdentity();
                    System.out.println(identity);
                    if (identity == 0) {
                        throw new Exception("request slice start, return identity == 0");
                    }
                    info.setSliceId(identity);
                    info.setSliceCount(count);

                    DBOperationUtils.updateMaterialDb(info);
                } else {
                    throw new Exception("request slice start, return data invalide");
                }
            } else {
                throw new Exception("request slice start, responseCode : " + resCode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        } finally {
            closeIo(conn, dos);
        }

        return new UploadFirstResponse(identity, count, "");
    }

    private static int uploadSecondStep(UploadFirstResponse firstResponse,
                                        MediaInfo info, UploadConfiguration configuration) throws Exception {
        // TODO: 2017/4/21 计算处理还未上传的片数
        List<Integer> successIds = firstResponse.getSuccessUploadIds();
        List<Integer> noUploadIds = firstResponse.getNotUploadIds();
        if (noUploadIds.size() == 0) {
            return firstResponse.identity;
        }

        HttpsURLConnection conn = null;
        DataOutputStream dos = null;
        int identity = -1;

        try {
            for (int i = 0; i < noUploadIds.size(); i++) {
                conn = getHttpsUrlConnection(UrlManager.UPLOAD_SLICE_UPLOAD, info.getMime_type(), configuration);
                dos = new DataOutputStream(conn.getOutputStream());
                UploadWriteUtils.writeParams(ParamsHelper.getSliceUploadParams(firstResponse.identity, noUploadIds.get(i)), dos, configuration);
                UploadWriteUtils.writeFile(i + 1, info.getAbsolutePath(), dos, configuration);

                int resCode = conn.getResponseCode();
                if (resCode == 200) {
                    String content = BaseUtils.inputStreamToString(conn.getInputStream());
                    UploadChunksStartResponse response = JSON.parseObject(content, UploadChunksStartResponse.class);

                    if (response != null) {
                        if (response.getErrno() == 0) {
                            identity = response.getData().getIdentity();
                            successIds.add(noUploadIds.get(i));
                        } else if (response.getErrno() == -1) {
                            //其他失败
                            throw new Exception("request upload slice, response erron: 1");
                        } else if (response.getErrno() == -2) {
                            //切片的所属对应文件标识不存在, 需要重新执行第一步上传操作
                            successIds.clear();
                            info.setSliceId(0);
                            info.setSliceCount(0);
                            DBOperationUtils.updateMaterialDb(info);
                            throw new Exception("request upload slice, response erron: 2");
                        }
                    }
                } else {
                    throw new Exception("request upload slice, responseCode : " + resCode);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        } finally {
            closeIo(conn, dos);
            //将上传成功的Id列表保存到数据库
            if (successIds.size() > 0) {
                info.setSuccessIds(listToStr(successIds));
                DBOperationUtils.updateMaterialDb(info);
            }
        }

        return identity;
    }

    private static String uploadThreeStep(int identity, MediaInfo info, UploadConfiguration configuration) throws Exception {
        if (identity < 0) {
            return "";
        }

        String body = "";
        HttpsURLConnection conn = null;
        DataOutputStream dos = null;

        try {
            conn = getHttpsUrlConnection(UrlManager.UPLOAD_SLICE_END, info.getMime_type(), configuration);
            dos = new DataOutputStream(conn.getOutputStream());
            UploadWriteUtils.writeParams(ParamsHelper.getEndUploadParams(info, identity), dos, configuration);

            int resCode = conn.getResponseCode();
            if (resCode == 200) {
                body = BaseUtils.inputStreamToString(conn.getInputStream());
            } else {
                throw new Exception("request upload slice end, responseCode : " + resCode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeIo(conn, dos);
        }

        return body;
    }

    private static void closeIo(HttpsURLConnection conn, DataOutputStream dos) {
        try {
            if (dos != null) {
                dos.close();
                dos = null;
            }

            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String listToStr(List<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Integer id : ids) {
            sb.append(id);
            sb.append(",");
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
