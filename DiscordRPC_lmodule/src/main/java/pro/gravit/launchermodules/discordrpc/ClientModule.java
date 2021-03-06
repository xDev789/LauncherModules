package pro.gravit.launchermodules.discordrpc;

import pro.gravit.launcher.Launcher;
import pro.gravit.launcher.client.events.ClientExitPhase;
import pro.gravit.launcher.client.events.ClientPreGuiPhase;
import pro.gravit.launcher.client.events.client.ClientProcessBuilderParamsWrittedEvent;
import pro.gravit.launcher.client.events.client.ClientProcessLaunchEvent;
import pro.gravit.launcher.modules.LauncherInitContext;
import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launcher.modules.LauncherModuleInfo;
import pro.gravit.launcher.request.Request;
import pro.gravit.utils.Version;
import pro.gravit.utils.helper.CommonHelper;
import pro.gravit.utils.helper.LogHelper;

public class ClientModule extends LauncherModule {
    public static final Version version = new Version(1, 1, 0, 1, Version.Type.LTS);

    public ClientModule() {
        super(new LauncherModuleInfo("DiscordRPC", version));
    }

    @Override
    public void init(LauncherInitContext initContext) {

        registerEvent(this::clientInit, ClientProcessLaunchEvent.class);
        registerEvent(this::launcherInit, ClientPreGuiPhase.class);
        registerEvent(this::exitHandler, ClientExitPhase.class);
        registerEvent(this::exitByStartClient, ClientProcessBuilderParamsWrittedEvent.class);
    }

    private String replace(String src, String nick, String title) {
        if (src == null) return null;
        return CommonHelper.replace(src, "user", nick, "profile", title);
    }

    private void clientInit(ClientProcessLaunchEvent phase) {
        CommonHelper.newThread("Discord RPC Thread", true, () -> {
            try {
                DiscordRPC.parameters.username = phase.params.playerProfile.username;
                DiscordRPC.parameters.userUUID = phase.params.playerProfile.uuid.toString();
                DiscordRPC.parameters.profileName = phase.params.profile.getTitle();
                DiscordRPC.parameters.minecraftVersion = phase.params.profile.getVersion().name;
                Config c = new Config();
                DiscordRPC.onConfig(c.appId, c.firstLine, c.secondLine, c.largeKey, c.smallKey, c.largeText, c.smallText);
                RequestEventWatcher.INSTANCE = new RequestEventWatcher(true);
                Request.service.registerEventHandler(RequestEventWatcher.INSTANCE);
            } catch (Throwable e) {
                LogHelper.error(e);
            }
        }).start();
    }

    private void launcherInit(ClientPreGuiPhase phase)
    {
        CommonHelper.newThread("Discord RPC Thread", true, () -> {
            try {
                Config c = new Config();
                if(!c.useAlt) return;
                DiscordRPC.onConfig(c.altAppId, c.altFirstLine, c.altSecondLine, c.altLargeKey, c.altSmallKey, c.altLargeText, c.altSmallText);
                RequestEventWatcher.INSTANCE = new RequestEventWatcher(false);
                Request.service.registerEventHandler(RequestEventWatcher.INSTANCE);
            } catch (Throwable e) {
                LogHelper.error(e);
            }
        }).start();
    }
    private void exitHandler(ClientExitPhase phase)
    {
        if(isClosed) return;
        if(DiscordRPC.thr != null) DiscordRPC.thr.interrupt();
        if(DiscordRPC.lib != null) DiscordRPC.lib.Discord_Shutdown();
        if(RequestEventWatcher.INSTANCE != null) Request.service.unregisterEventHandler(RequestEventWatcher.INSTANCE);
        isClosed = true;
    }
    private boolean isClosed = false;
    private void exitByStartClient(ClientProcessBuilderParamsWrittedEvent event)
    {
        if(isClosed) return;
        try {
            if(DiscordRPC.thr != null) DiscordRPC.thr.interrupt();
            if(DiscordRPC.lib != null) DiscordRPC.lib.Discord_Shutdown();
            if(RequestEventWatcher.INSTANCE != null) Request.service.unregisterEventHandler(RequestEventWatcher.INSTANCE);
            isClosed = true;
        } catch (Throwable ignored)
        {

        }
    }

    public static void main(String[] args) {
        System.err.println("This is module, use with GravitLauncher`s Launcher.");
    }

}
