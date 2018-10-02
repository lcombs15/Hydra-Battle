package test;

import main.HydraNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HydraNodeTest {

    private HydraNode SUT;

    @BeforeEach
    public void setup() {
        //Reset system under test each time
        SUT = null;
    }

    @Test
    public void givenHydraNode_whenConstructorCalledWithBooleanTrue_thenIsBodyIsTrue() {
        SUT = new HydraNode(true);
        assertTrue(SUT.isHydraBody());
    }

    @Test
    void givenHydraNodeWithChildren_whenCanbeChoppedCalled_thenFalse() {
        SUT = new HydraNode(false);
        SUT.addChild(new HydraNode());
        assertFalse(SUT.canBeCopped());
    }

    @Test
    void givenHydraNode_whenAddChildPassedNodeThatIsaBody_thenException() {
        SUT = new HydraNode(false);
        assertThrows(UnsupportedOperationException.class, () -> {
            SUT.addChild(new HydraNode(true));
        });
    }
}