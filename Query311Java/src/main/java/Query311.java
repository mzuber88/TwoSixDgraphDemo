import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.protobuf.ByteString;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.DgraphGrpc.DgraphStub;
import io.dgraph.DgraphProto.Mutation;
import io.dgraph.DgraphProto.Operation;
import io.dgraph.DgraphProto.Response;
import io.dgraph.Transaction;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

public class Query311 {
  private static final String TEST_HOSTNAME = "localhost";
  private static final int TEST_PORT = 9080;

  private static DgraphClient createDgraphClient(boolean withAuthHeader) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(TEST_HOSTNAME, TEST_PORT).usePlaintext(true).build();
    DgraphStub stub = DgraphGrpc.newStub(channel);

    if (withAuthHeader) {
      Metadata metadata = new Metadata();
      metadata.put(
          Metadata.Key.of("auth-token", Metadata.ASCII_STRING_MARSHALLER), "the-auth-token-value");
      stub = MetadataUtils.attachHeaders(stub, metadata);
    }

    return new DgraphClient(stub);
  }

  private static void prettyPrintQueryResponse(Response res)
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonParser jp = new JsonParser();
    JsonElement je = jp.parse(res.getJson().toStringUtf8());
    String prettyAnswer = gson.toJson(je);
    System.out.println(prettyAnswer);
  }

  public static void main(final String[] args) {
    DgraphClient dgraphClient = createDgraphClient(false);
    String query;
    Response res;

    query = "{me(func: has(agency)) @filter(ge(closed_date, \"2018\")) {descriptor borough latitude longitude}}";
    System.out.println("Query nodes that have agency property and closed date > 2018, return predicates descriptor, borough, latitude, and longitude");
    System.out.println("Raw Query:" + query);
    System.out.println("Response:");
    res = dgraphClient.newTransaction().query(query);
    prettyPrintQueryResponse(res);
    System.out.println("");

    query = "{me(func: has(borough)) @filter(ge(longitude, -74) AND ge(latitude, 40.9) ) {descriptor borough latitude longitude}}";
    System.out.println("Query nodes that have borough property and are located at a longitude > -74 and latitude > 40.9, return predicates descriptor, borough, latitude, and longitude");
    System.out.println("Raw Query:" + query);
    System.out.println("Response:");
    res = dgraphClient.newTransaction().query(query);
    prettyPrintQueryResponse(res);
  }

}
