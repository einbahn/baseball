import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import java.util.LinkedList;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.StdOut;

class BaseballElimination {
    private int n;
    private LinkedList<String> t = new LinkedList<>();
    private int[] w;
    private int[] l;
    private int[] r;
    private int[][] g;

    public BaseballElimination(String filename) {
        if (filename == null)
            throw new IllegalArgumentException("Argument to constructor is null");
        In in = new In(filename);
        n = in.readInt();
        w = new int[n];
        l = new int[n];
        r = new int[n];
        g = new int[n][n];
        for (int i = 0; i < n; i++) {
            t.add(in.readString());
            w[i] = in.readInt();
            l[i] = in.readInt();
            r[i] = in.readInt();
            for (int j = 0; j < n; j++) {
                g[i][j] = in.readInt();
            }
        }
    }

    public int numberOfTeams() {
        return n;
    }

    public Iterable<String> teams() {
        return t;
    }

    public int wins(String team) {
        if (team == null || !t.contains(team))
            throw new IllegalArgumentException("The argument to method wins is null or invalid");
        return w[t.indexOf(team)];
    }

    public int losses(String team) {
        if (team == null || !t.contains(team))
            throw new IllegalArgumentException("The argument to method losses is null or invalid");
        return l[t.indexOf(team)];
    }

    public int remaining(String team) {
        if (team == null || !t.contains(team))
            throw new IllegalArgumentException("The argument to method remaining is null or invalid");
        return r[t.indexOf(team)];
    }

    public int against(String team1, String team2) {
        if (team1 == null || !t.contains(team1) || team2 == null || !t.contains(team2))
            throw new IllegalArgumentException("The argument to against is invalid");
        return g[t.indexOf(team1)][t.indexOf(team2)];
    }

    private FlowNetwork getFlowNetwork(String team) {
        int x = t.indexOf(team);
        int gvn = (n - 1) * (n - 2) / 2;
        int nv = 1 + gvn + n;
        FlowNetwork fn = new FlowNetwork(nv);
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
                fn.addEdge(new FlowEdge(s, counter, g[i][j]));
                int offsetX = teamVertexBase + i;
                int offsetY = teamVertexBase + j;
                if (i > x) {
                    offsetX--;
                }
                if (j > x) {
                    offsetY--;
                }
                fn.addEdge(new FlowEdge(counter, offsetY, Double.POSITIVE_INFINITY));
                fn.addEdge(new FlowEdge(counter, offsetX, Double.POSITIVE_INFINITY));
            }
        }
        counter = 0;
        for (int i = 0; i < n; i++) {
            if (i == x)
                continue;
            fn.addEdge(new FlowEdge(teamVertexBase + counter, t, w[x] + r[x] - w[i]));
            counter++;
        }
        return fn;
    }

    public boolean isEliminated(String team) {
        if (team == null || !t.contains(team))
            throw new IllegalArgumentException("The argument to method isEliminated is null or invalid");
        int x = t.indexOf(team);
        for (int i = 0; i < n; i++) {
            if (i == x)
                continue;
            if (w[x] + r[x] < w[i])
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
        if (team == null || !t.contains(team))
            throw new IllegalArgumentException("The argument to method certificateOfELimination is null or invalid");
        int teamIndex = t.indexOf(team);
        LinkedList<String> certificate = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (i == teamIndex)
                continue;
            if (w[teamIndex] + r[teamIndex] < w[i]) {
                certificate.add(t.get(i));
                return certificate;
            }
        }
        FlowNetwork g = getFlowNetwork(team);
        FordFulkerson ff = new FordFulkerson(g, 0, g.V() - 1);
        int nGameVertices = (n - 1) * (n - 2) / 2;
        int index = 0;
        for (int i = 0; i < n; i++) {
            if (i == teamIndex)
                continue;
            if (ff.inCut(1 + nGameVertices + index)) {
                certificate.add(t.get(i));
            }
            index++;
        }
        return certificate;
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

