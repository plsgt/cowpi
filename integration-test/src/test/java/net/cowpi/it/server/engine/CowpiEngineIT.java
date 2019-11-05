package net.cowpi.it.server.engine;

import org.junit.Test;

public class CowpiEngineIT {

    @Test
    public void testSetupConversation(){
        CowpiEngineComponent component = DaggerCowpiEngineComponent.create();

        component.database().clean();
        component.database().migrate();
    }
}
