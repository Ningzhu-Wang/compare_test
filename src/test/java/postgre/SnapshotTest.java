package postgre;

import benchmark.client.DBProxy;
import benchmark.client.PostgreSQLExecutorClient;
import benchmark.transaction.definition.SnapshotQueryTx;
import benchmark.transaction.generation.BenchmarkTxResultProcessor;
import benchmark.utils.Helper;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SnapshotTest {

    private static int threadCnt = Integer.parseInt(Helper.mustEnv("MAX_CONNECTION_CNT")); // number of threads to send queries.
    private static String serverHost = Helper.mustEnv("DB_HOST"); // hostname of postgre server.
    private static String resultFile = Helper.mustEnv("SERVER_RESULT_FILE");

    private static Producer logger;
    private static DBProxy client;
    private static BenchmarkTxResultProcessor post;

    @BeforeClass
    public static void initClient() throws IOException, ExecutionException, InterruptedException, SQLException, ClassNotFoundException {
        logger = Helper.getLogger();
        client = new PostgreSQLExecutorClient(serverHost, threadCnt, 800);
        post = new BenchmarkTxResultProcessor("Postgre(SnapShotQuery)", Helper.codeGitVersion());
        post.setLogger(logger);
        post.setResult(new File(resultFile));
    }

    @Test
    public void test() throws Exception {
        ArrayList<String[]> parametersList = Helper.csvReader("snapshot_parameters.csv");
        for (int i = 0; i < 100; i++) {
            String startTime = parametersList.get(i)[1];
            query("travel_t", Helper.timeStr2int(startTime));
        }
    }

    private void query(String propertyName, int t) throws Exception {
        SnapshotQueryTx tx = new SnapshotQueryTx();
        tx.setPropertyName(propertyName);
        tx.setTimestamp(t);
        post.process(client.execute(tx), tx);
    }

    @AfterClass
    public static void close() throws IOException, InterruptedException, ProducerException {
        client.close();
        Thread.sleep(1000 * 60 * 2);
        post.close();
        logger.close();
    }
}
