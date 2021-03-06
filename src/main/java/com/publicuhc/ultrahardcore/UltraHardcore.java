/*
 * UltraHardcore.java
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Graham Howden <graham_howden1 at yahoo.co.uk>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.publicuhc.ultrahardcore;

import com.publicuhc.pluginframework.FrameworkJavaPlugin;
import com.publicuhc.pluginframework.routing.Router;
import com.publicuhc.pluginframework.routing.exception.CommandParseException;
import com.publicuhc.pluginframework.shaded.inject.*;
import com.publicuhc.pluginframework.shaded.metrics.Metrics;
import com.publicuhc.ultrahardcore.addons.FeatureManager;
import com.publicuhc.ultrahardcore.addons.ServicesModule;
import com.publicuhc.ultrahardcore.addons.SharedServicesModule;
import com.publicuhc.ultrahardcore.api.Command;
import com.publicuhc.ultrahardcore.api.Feature;
import com.publicuhc.ultrahardcore.api.UHCAddonConfiguration;
import com.publicuhc.ultrahardcore.api.UHCFeature;
import com.publicuhc.ultrahardcore.api.exceptions.FeatureIDConflictException;
import com.publicuhc.ultrahardcore.core.UHCCoreAddonConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * UltraHardcore
 * <p/>
 * Main plugin class, init
 *
 * @author ghowden
 */
@Singleton
public class UltraHardcore extends FrameworkJavaPlugin
{

    private FeatureManager featureManager;
    private Router router;
    private Metrics metrics;
    private SharedServicesModule sharedServicesModule;

    //When the plugin gets started
    @Override
    protected void onFrameworkEnable()
    {
        //load the core addon
        registerAddon(this, new UHCCoreAddonConfiguration());

        //enable metrics
        Metrics.Graph graph = metrics.createGraph("Features Loaded");
        for(final Feature feature : featureManager.getFeatures()) {
            graph.addPlotter(new Metrics.Plotter(feature.getFeatureID())
            {
                @Override
                public int getValue()
                {
                    return feature.isEnabled() ? 1 : 0;
                }
            });
        }
        metrics.addGraph(graph);
        metrics.start();
    }

    @Inject
    private void setSharedServicesModule(SharedServicesModule module)
    {
        sharedServicesModule = module;
    }

    @Inject
    private void setMetrics(Metrics metrics)
    {
        this.metrics = metrics;
    }

    @SuppressWarnings("UnusedDeclaration")
    public FeatureManager getFeatureManager()
    {
        return featureManager;
    }

    @Inject
    private void setFeatureManager(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    public Router getRouter()
    {
        return router;
    }

    @Inject
    private void setRouter(Router router)
    {
        this.router = router;
    }

    @Override
    protected void initialModules(List<Module> modules)
    {
        modules.addAll(getDefaultModules());
        modules.add(new UHCModule());
    }

    /**
     * Register an addon module for features/commands without any extra modules
     *
     * @param plugin        the plugin to register the addon for
     * @param configuration the configuration which tells us the commands/features
     */
    public void registerAddon(Plugin plugin, UHCAddonConfiguration configuration)
    {
        registerAddon(plugin, configuration, new ArrayList<Module>());
    }

    /**
     * Register an addon module for features/commands
     *
     * @param plugin        the plugin to register the addon for
     * @param configuration the configuration which tells us the commands/features
     * @param modules       list of extra modules to add to the injector
     */
    @SuppressWarnings({"AnonymousInnerClass", "EmptyClass"})
    public void registerAddon(Plugin plugin, UHCAddonConfiguration configuration, Collection<Module> modules)
    {
        modules.add(sharedServicesModule);
        modules.add(new ServicesModule(plugin, configuration));

        Injector injector = Guice.createInjector(modules);

        //fetch all of the features from the addon and add it to the feature manager
        Set<UHCFeature> features = injector.getInstance(Key.get(new TypeLiteral<Set<UHCFeature>>() {}));
        for(UHCFeature feature : features) {
            try {
                featureManager.addFeature(feature);
            } catch(FeatureIDConflictException e) {
                e.printStackTrace();
            }
        }

        //fetch all of the commands from the addon and add it to the router
        Set<Command> commands = injector.getInstance(Key.get(new TypeLiteral<Set<Command>>() {}));
        for(Command command : commands) {
            try {
                getRouter().registerCommands(command, false);
            } catch(CommandParseException e) {
                e.printStackTrace();
            }
        }
    }
}