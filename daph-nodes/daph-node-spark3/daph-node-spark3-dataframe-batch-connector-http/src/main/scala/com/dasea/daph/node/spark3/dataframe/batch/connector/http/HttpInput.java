package com.dasea.daph.node.spark3.dataframe.batch.connector.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dasea.daph.api.config.NodeConfig;
import com.dasea.daph.node.spark3.dataframe.batch.connector.http.util.HttpClientResult;
import com.dasea.daph.node.spark3.dataframe.batch.connector.http.util.HttpClientUtils;
import com.dasea.daph.spark3.api.node.dataframe.connector.input.DataFrameSingleInput;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpInput extends DataFrameSingleInput {
    private Logger LOG = LogManager.getLogger(HttpInput.class);
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final int INITIAL_CAPACITY = 16;

    @Override
    public Dataset<Row> in() {
        HttpInputConfig config = (HttpInputConfig) nodeConfig();

        SparkSession spark = spark();
        String url = config.getUrl();
        String method = config.getMethod();
        String header = config.getHeader();
        String requestParams = config.getRequestParams();
        String syncPath = config.getSyncPath();

        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());

        Map requestMap = jsonToMap(requestParams);
        String syncValues = getSyncValues(jsc, syncPath);
        Map syncMap = jsonToMap(syncValues);
        if (!syncMap.isEmpty()) {
            requestMap.putAll(syncMap);
        }

        HttpClientResult response = new HttpClientResult();
        try {
            Map headerMap = jsonToMap(header);
            if (POST.equals(method)) {
                response = HttpClientUtils.doPost(url, headerMap, requestMap);
            } else {
                response = HttpClientUtils.doGet(url, headerMap, requestMap);
            }
        } catch (Exception e) {
            LOG.error("http call error!", e);
        }

        LOG.info("http respond code->" + response.getCode());

        List<String> array = new ArrayList<>();
        array.add(response.getContent());
        JavaRDD<String> javaRDD = (JavaRDD<String>) jsc.parallelize(array);
        DataFrameReader reader = spark.read().format("json");
        return reader.json(javaRDD);
    }

    private String getSyncValues(JavaSparkContext jsc, String syncPath) {
        if (null == syncPath || syncPath.isEmpty()) {
            return "";
        }
        Configuration hadoopConf = jsc.hadoopConfiguration();
        List<String> values = new ArrayList<>();
        try {
            FileSystem fs = FileSystem.get(hadoopConf);
            Path path = new Path(syncPath);
            boolean exists = fs.exists(path);
            if (exists) {
                JavaRDD<String> checkPoint = jsc.textFile(syncPath);
                values = checkPoint.collect();

            }
        } catch (IOException e) {
            String msg = "getSyncValues error, syncPath is {}" + syncPath;
            LOG.error(msg, e);
        }
        return values.isEmpty() ? "" : values.iterator().next();
    }

    private Map jsonToMap(String content) {
        Map map = new HashMap<>(INITIAL_CAPACITY);
        if (null == content || content.isEmpty()) {
            return map;
        }

        try {
            return new ObjectMapper().readValue(content, HashMap.class);
        } catch (IOException e) {
            //only records the log, does not handle it, and does not affect the main process.
            String msg = content + " json to map error!";
            LOG.error(msg, e);
        }
        return map;
    }

    @Override
    public Class<? extends NodeConfig> getNodeConfigClass() {
        return HttpInputConfig.class;
    }
}
