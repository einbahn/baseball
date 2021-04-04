import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import java.util.LinkedList;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {
    private final int n;
    private final LinkedList<String> teams = new LinkedList<>();
    private final int[] wins;
    private final int[] losses;
    private final int[] remain;
    private final int[][] against;

    public BaseballElimination(String filename) {
        if (filename == null)
            throw new IllegalArgumentException("Argument to constructor is null");
        In in = new In(filename);
        n = in.readInt();
        wins = new int[n];
        losses = new int[n];
        remain = new int[n];
        against = new int[n][n];
        for (int i = 0; i < n; i++) {
            teams.add(in.readString());
            wins[i] = in.readInt();
            losses[i] = in.readInt();
            remain[i] = in.readInt();
            for (int j = 0; j < n; j++) {
                against[i][j] = in.readInt();
            }
        }
    }

    public int numberOfTeams() {
        return n;
    }

    public Iterable<String> teams() {
        return teams;
    }

    public int wins(String team) {
        if (team == null || !teams.contains(team))
            throw new IllegalArgumentException("The argument to method wins is null or invalid");
        return wins[teams.indexOf(team)];
    }

    public int losses(String team) {
        if (team == null || !teams.contains(team))
            throw new IllegalArgumentException("The argument to method losses is null or invalid");
        return losses[teams.indexOf(team)];
    }

    public int remaining(String team) {
        if (team == null || !teams.contains(team))
            throw new IllegalArgumentException("The argument to method remaining is null or invalid");
        return remain[teams.indexOf(team)];
    }

    public int against(String team1, String team2) {
        if (team1 == null || !teams.contains(team1) || team2 == null || !teams.contains(team2))
            throw new IllegalArgumentException("The argument to against is invalid");
        return against[teams.indexOf(team1)][teams.indexOf(team2)];
    }

    private FlowNetwork getFlowNetwork(String team) {
        int x = teams.indexOf(team);
        int gvn = (n - 1) * (n - 2) / 2;
        int nv = 1 + gvn + n;
        FlowNetwork G = new FlowNetwork(nv);
        int s = 0;
        int t = nv - 1;
        int counter = 0;
        int teamVertexBase = gvn + 1;
        for (int i = 0; i < n; i++) {
            if (i == x)
                continue;
            for (int j = i + 1; j < n; j++) {
                if (j == x)
                    continue;
                counter++;
                G.addEdge(new FlowEdge(s, counter, against[i][j]));
                int offsetX = teamVertexBase + i;
                int offsetY = teamVertexBase + j;
                if (i > x) {
                    offsetX--;
                }
                if (j > x) {
                    offsetY--;
                }
                G.addEdge(new FlowEdge(counter, offsetY, Double.POSITIVE_INFINITY));
                G.addEdge(new FlowEdge(counter, offsetX, Double.POSITIVE_INFINITY));
            }
        }
        counter = 0;
        for (int i = 0; i < n; i++) {
            if (i == x)
                continue;
            G.addEdge(new FlowEdge(teamVertexBase + counter, t, wins[x] + remain[x] - wins[i]));
            counter++;
        }
        return G;
    }

    public boolean isEliminated(String team) {
        if (team == null || !teams.contains(team))
            throw new IllegalArgumentException("The argument to method isEliminated is null or invalid");
        int x = teams.indexOf(team);
        for (int i = 0; i < n; i++) {
            if (i == x)
                continue;
            if (wins[x] + remain[x] < wins[i])
                return true;
        }
        FlowNetwork G = getFlowNetwork(team);
        FordFulkerson ff = new FordFulkerson(G, 0, G.V() - 1);
        int sourceCapacity = 0;
        for (FlowEdge e : G.adj(0)) {
            sourceCapacity += e.capacity();
        }
        return ff.value() < sourceCapacity;
    }

    public Iterable<String> certificateOfElimination(String team) {
        if (team == null || !teams.contains(team))
            throw new IllegalArgumentException("The argument to method certificateOfELimination is null or invalid");
        int teamIndex = teams.indexOf(team);
        LinkedList<String> certificate = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (i == teamIndex)
                continue;
            if (wins[teamIndex] + remain[teamIndex] < wins[i]) {
                certificate.add(teams.get(i));
                return certificate;
            }
        }
        FlowNetwork G = getFlowNetwork(team);
        FordFulkerson ff = new FordFulkerson(G, 0, G.V() - 1);
        int nGameVertices = (n - 1) * (n - 2) / 2;
        int index = 0;
        for (int i = 0; i < n; i++) {
            if (i == teamIndex)
                continue;
            if (ff.inCut(1 + nGameVertices + index)) {
                certificate.add(teams.get(i));
            }
            index++;
        }
        if (certificate.isEmpty()) return null;
        else return certificate;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}

