package postgre;

import benchmark.client.DBProxy;
import benchmark.client.MariaDBExecutorClient;
import benchmark.client.PostgreSQLExecutorClient;
import benchmark.transaction.definition.ReachableAreaQueryTx;
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
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ReachableAreaTest {
    private static int threadCnt = Integer.parseInt(Helper.mustEnv("MAX_CONNECTION_CNT"));
    private static String serverHost = Helper.mustEnv("DB_HOST");
    private static String resultFile = Helper.mustEnv("SERVER_RESULT_FILE");

    private static Producer logger;
    private static DBProxy client;
    private static BenchmarkTxResultProcessor post;

    @BeforeClass
    public static void initClient() throws IOException, ExecutionException, InterruptedException, SQLException, ClassNotFoundException {
        // logger = Helper.getLogger();
        client = new PostgreSQLExecutorClient(serverHost, threadCnt, 800);
        post = new BenchmarkTxResultProcessor("PostgreSQL(ReachableAreaQuery)", Helper.codeGitVersion());
        // post.setLogger(logger);
        post.setResult(new File(resultFile));
    }

    @Test
    public void test() throws Exception {
        ArrayList<String[]> parametersList = Helper.csvReader("reachable_area_parameters.csv");
        for (int i = 0; i < 100; i++) {
            long id = Integer.parseInt(parametersList.get(i)[1]);
            String st = parametersList.get(i)[2];
            query(id, Helper.timeStr2int(st), 3600);
        }
    }

    private void query(long ID, int st, int tt) throws Exception {
        ReachableAreaQueryTx tx = new ReachableAreaQueryTx();
        tx.setStartCrossId(ID);
        tx.setDepartureTime(st);
        tx.setTravelTime(tt);
        post.process(client.execute(tx), tx);
    }

    @AfterClass
    public static void close() throws IOException, InterruptedException, ProducerException {
        client.close();
        Thread.sleep(1000 * 30);
        post.close();
        // logger.close();
    }
}
