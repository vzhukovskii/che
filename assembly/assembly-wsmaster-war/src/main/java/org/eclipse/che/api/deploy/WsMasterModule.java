/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.deploy;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.api.agent.GitCredentialsAgent;
import org.eclipse.che.api.agent.LSCSharpAgent;
import org.eclipse.che.api.agent.LSJsonAgent;
import org.eclipse.che.api.agent.LSPhpAgent;
import org.eclipse.che.api.agent.LSPythonAgent;
import org.eclipse.che.api.agent.LSTypeScriptAgent;
import org.eclipse.che.api.agent.LSYamlAgent;
import org.eclipse.che.api.agent.SshAgent;
import org.eclipse.che.api.agent.SshAgentLauncher;
import org.eclipse.che.api.agent.UnisonAgent;
import org.eclipse.che.api.agent.WsAgent;
import org.eclipse.che.api.agent.WsAgentLauncher;
import org.eclipse.che.api.agent.server.launcher.AgentLauncher;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.MessageBodyAdapter;
import org.eclipse.che.api.core.rest.MessageBodyAdapterInterceptor;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.machine.server.jpa.MachineJpaModule;
import org.eclipse.che.api.machine.server.recipe.RecipeLoader;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.workspace.server.WorkspaceConfigMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.WorkspaceMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.api.workspace.server.stack.StackMessageBodyAdapter;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.github.factory.resolver.GithubFactoryParametersResolver;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

/** @author andrew00x */
@DynaModule
public class WsMasterModule extends AbstractModule {
  @Override
  protected void configure() {
    // db related components modules
    install(new com.google.inject.persist.jpa.JpaPersistModule("main"));
    install(new org.eclipse.che.account.api.AccountModule());
    install(new org.eclipse.che.api.ssh.server.jpa.SshJpaModule());
    install(new MachineJpaModule());
    install(new org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule());
    install(new org.eclipse.che.api.core.websocket.impl.WebSocketModule());

    // db configuration
    bind(SchemaInitializer.class)
        .to(org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer.class);
    bind(org.eclipse.che.core.db.DBInitializer.class).asEagerSingleton();
    bind(PlaceholderReplacer.class)
        .toProvider(org.eclipse.che.core.db.schema.impl.flyway.PlaceholderReplacerProvider.class);

    //factory
    bind(FactoryAcceptValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
    bind(FactoryCreateValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
    bind(FactoryEditValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
    bind(org.eclipse.che.api.factory.server.FactoryService.class);
    install(new org.eclipse.che.api.factory.server.jpa.FactoryJpaModule());

    Multibinder<FactoryParametersResolver> factoryParametersResolverMultibinder =
        Multibinder.newSetBinder(binder(), FactoryParametersResolver.class);
    factoryParametersResolverMultibinder.addBinding().to(GithubFactoryParametersResolver.class);

    install(new org.eclipse.che.plugin.docker.compose.ComposeModule());

    bind(org.eclipse.che.api.core.rest.ApiInfoService.class);
    bind(org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.project.server.template.ProjectTemplateRegistry.class);
    bind(org.eclipse.che.api.project.server.template.ProjectTemplateService.class);
    bind(org.eclipse.che.api.ssh.server.SshService.class);
    bind(RecipeService.class);
    bind(org.eclipse.che.api.user.server.UserService.class);
    bind(org.eclipse.che.api.user.server.ProfileService.class);
    bind(org.eclipse.che.api.user.server.PreferencesService.class);

    MapBinder<String, String> stacks =
        MapBinder.newMapBinder(
            binder(), String.class, String.class, Names.named(StackLoader.CHE_PREDEFINED_STACKS));
    stacks.addBinding("stacks.json").toInstance("stacks-images");
    stacks.addBinding("che-in-che.json").toInstance("");
    bind(org.eclipse.che.api.workspace.server.stack.StackService.class);
    bind(org.eclipse.che.api.workspace.server.TemporaryWorkspaceRemover.class);
    bind(org.eclipse.che.api.workspace.server.WorkspaceService.class);
    bind(org.eclipse.che.api.workspace.server.event.WorkspaceMessenger.class).asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.WorkspaceJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.plugin.docker.machine.ext.DockerMachineExtServerChecker.class);
    bind(org.eclipse.che.plugin.docker.machine.ext.DockerMachineTerminalChecker.class);
    bind(org.eclipse.che.everrest.EverrestDownloadFileResponseFilter.class);
    bind(org.eclipse.che.everrest.ETagResponseFilter.class);
    bind(org.eclipse.che.api.agent.server.AgentRegistryService.class);

    bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);
    bind(org.eclipse.che.security.oauth.shared.OAuthTokenProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);
    bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);

    bind(org.eclipse.che.api.core.notification.WSocketEventBusServer.class);
    // additional ports for development of extensions
    Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers =
        Multibinder.newSetBinder(
            binder(),
            org.eclipse.che.api.core.model.machine.ServerConf.class,
            Names.named("machine.docker.dev_machine.machine_servers"));
    machineServers
        .addBinding()
        .toInstance(
            new org.eclipse.che.api.machine.server.model.impl.ServerConfImpl(
                Constants.WSAGENT_DEBUG_REFERENCE, "4403/tcp", "http", null));

    bind(RecipeLoader.class);
    Multibinder.newSetBinder(
            binder(), String.class, Names.named(RecipeLoader.CHE_PREDEFINED_RECIPES))
        .addBinding()
        .toInstance("predefined-recipes.json");

    bind(org.eclipse.che.api.workspace.server.WorkspaceValidator.class)
        .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceValidator.class);

    bind(org.eclipse.che.api.workspace.server.event.MachineStateListener.class).asEagerSingleton();

    // agents
    bind(org.eclipse.che.api.agent.server.AgentRegistry.class)
        .to(org.eclipse.che.api.agent.server.impl.AgentRegistryImpl.class);
    Multibinder<Agent> agents = Multibinder.newSetBinder(binder(), Agent.class);
    agents.addBinding().to(SshAgent.class);
    agents.addBinding().to(UnisonAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.ExecAgent.class);
    agents.addBinding().to(org.eclipse.che.api.agent.TerminalAgent.class);
    agents.addBinding().to(WsAgent.class);
    agents.addBinding().to(LSPhpAgent.class);
    agents.addBinding().to(LSPythonAgent.class);
    agents.addBinding().to(LSJsonAgent.class);
    agents.addBinding().to(LSYamlAgent.class);
    agents.addBinding().to(LSCSharpAgent.class);
    agents.addBinding().to(LSTypeScriptAgent.class);
    agents.addBinding().to(GitCredentialsAgent.class);

    Multibinder<AgentLauncher> launchers = Multibinder.newSetBinder(binder(), AgentLauncher.class);
    launchers.addBinding().to(WsAgentLauncher.class);
    launchers.addBinding().to(org.eclipse.che.api.agent.ExecAgentLauncher.class);
    launchers.addBinding().to(org.eclipse.che.api.agent.TerminalAgentLauncher.class);
    launchers.addBinding().to(SshAgentLauncher.class);

    bindConstant()
        .annotatedWith(Names.named("machine.ws_agent.run_command"))
        .to("export JPDA_ADDRESS=\"4403\" && ~/che/ws-agent/bin/catalina.sh jpda run");

    bind(org.eclipse.che.api.deploy.WsMasterAnalyticsAddresser.class);

    Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider>
        machineImageProviderMultibinder =
            Multibinder.newSetBinder(
                binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
    machineImageProviderMultibinder
        .addBinding()
        .to(org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.class);

    install(new org.eclipse.che.plugin.activity.inject.WorkspaceActivityModule());

    install(new org.eclipse.che.api.core.rest.CoreRestModule());
    install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
    install(new org.eclipse.che.plugin.docker.machine.local.LocalDockerModule());
    install(new org.eclipse.che.api.machine.server.MachineModule());
    install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());
    install(new org.eclipse.che.swagger.deploy.DocsModule());
    install(new org.eclipse.che.plugin.machine.ssh.SshMachineModule());
    install(new org.eclipse.che.plugin.docker.machine.proxy.DockerProxyModule());
    install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());

    final Multibinder<MessageBodyAdapter> adaptersMultibinder =
        Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(WorkspaceConfigMessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(WorkspaceMessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(StackMessageBodyAdapter.class);

    final MessageBodyAdapterInterceptor interceptor = new MessageBodyAdapterInterceptor();
    requestInjection(interceptor);
    bindInterceptor(subclassesOf(CheJsonProvider.class), names("readFrom"), interceptor);
    bind(org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner.class)
        .to(org.eclipse.che.plugin.docker.machine.cleaner.LocalWorkspaceFilesCleaner.class);
    bind(org.eclipse.che.api.environment.server.InfrastructureProvisioner.class)
        .to(org.eclipse.che.plugin.docker.machine.local.LocalCheInfrastructureProvisioner.class);

    // system components
    bind(org.eclipse.che.api.system.server.SystemService.class);
    bind(org.eclipse.che.api.system.server.SystemEventsWebsocketBroadcaster.class)
        .asEagerSingleton();

    install(new org.eclipse.che.plugin.docker.machine.dns.DnsResolversModule());
    install(new org.eclipse.che.plugin.traefik.TraefikDockerModule());

    bind(org.eclipse.che.api.agent.server.filters.AddExecAgentInWorkspaceFilter.class);
    bind(org.eclipse.che.api.agent.server.filters.AddExecAgentInStackFilter.class);

    bind(org.eclipse.che.api.workspace.server.idle.ServerIdleDetector.class);
  }
}
