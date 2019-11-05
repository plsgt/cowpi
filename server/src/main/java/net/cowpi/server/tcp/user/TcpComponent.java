package net.cowpi.server.tcp.user;

import dagger.BindsInstance;
import dagger.Subcomponent;
import net.cowpi.server.tcp.TcpServerHandler;

@Subcomponent
@UserScope
public interface TcpComponent {
    TcpServerHandler serverHandler();

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance Builder username(@Username String username);
        @BindsInstance Builder userId(@UserId long id);
        TcpComponent build();
    }

}
