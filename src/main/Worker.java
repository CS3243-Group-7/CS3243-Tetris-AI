package main;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

public class Worker {
    Worker() {
        try {
            JChannel channel = new JChannel("~/tetrisCommunicationData/game.xml");
            channel.setReceiver(new ReceiverAdapter() {
                public void receive(Message msg) {
                    System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
                }
            });
            channel.connect("compg9");
            channel.send(new Message(null, "hello world"));
            channel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int runGame(double[] featureWeights) {
        State s = new State();
        PlayerSkeleton p = new PlayerSkeleton(featureWeights);
        while (!s.hasLost()) {
            s.makeMove(p.pickMove(s, s.legalMoves()));
        }
        return s.getRowsCleared();
    }
}
