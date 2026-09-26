package org.elemental_reaction;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.elemental_reaction.client.FrozenEntityRenderer;
import org.elemental_reaction.effect.ElementalVulnerableEffect;
import org.elemental_reaction.effect.FrozenEffect;
import org.elemental_reaction.effect.ImbalancedEffect;
import org.elemental_reaction.event.BurningParticles;
import org.elemental_reaction.event.BurningReactionEffect;
import org.elemental_reaction.event.ElementDiffusionParticles;
import org.elemental_reaction.event.ElementExplosionHandler;
import org.elemental_reaction.event.FreezeParticleEffect;
import org.elemental_reaction.event.FreezeParticles;
import org.elemental_reaction.event.MudflowParticleEffect;
import org.elemental_reaction.event.MudflowParticles;
import org.elemental_reaction.event.SoulScorchParticleEffect;
import org.elemental_reaction.event.SoulScorchParticles;
import org.elemental_reaction.event.TurbulenceParticles;
import org.elemental_reaction.event.TurbulenceParticleEffect;
import org.elemental_reaction.particle.ReactionTextParticleOptions;
import org.elemental_reaction.particle.ReactionTextParticleType;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Elemental_reaction.MODID)
public class Elemental_reaction {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "elemental_reaction";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "elemental_reaction" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "elemental_reaction" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "elemental_reaction" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    // Creates a new Block with the id "elemental_reaction:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "elemental_reaction:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Creates a new food item with the id "elemental_reaction:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));

    // Creates a creative tab with the id "elemental_reaction:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
    }).build());
    public static final RegistryObject<SimpleParticleType> ELEMENT_ATTACHMENT_PARTICLE = PARTICLE_TYPES.register(
            "element_attachment",
            () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> ELEMENT_DIFFUSION_PARTICLE = PARTICLE_TYPES.register(
            "element_diffusion",
            () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> MUD_BLOB_PARTICLE = PARTICLE_TYPES.register(
            "mud_blob",
            () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> MUD_CHUNK_PARTICLE = PARTICLE_TYPES.register(
            "mud_chunk",
            () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> SWAMP_MOTE_PARTICLE = PARTICLE_TYPES.register(
            "swamp_mote",
            () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> WATER_DROP_PARTICLE = PARTICLE_TYPES.register(
            "water_drop",
            () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> MUD_RING_PARTICLE = PARTICLE_TYPES.register(
            "mud_ring",
            () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> TURBULENCE_ARC_PARTICLE = PARTICLE_TYPES.register(
            "turbulence_arc", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> TURBULENCE_TIDE_DROP_PARTICLE = PARTICLE_TYPES.register(
            "turbulence_tide_drop", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> TURBULENCE_WATER_DROP_PARTICLE = PARTICLE_TYPES.register(
            "turbulence_water_drop", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> TURBULENCE_DARK_SPARK_PARTICLE = PARTICLE_TYPES.register(
            "turbulence_dark_spark", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> TURBULENCE_BLACK_SMOKE_PARTICLE = PARTICLE_TYPES.register(
            "turbulence_black_smoke", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> BURNING_FLAME_PARTICLE = PARTICLE_TYPES.register(
            "burning_flame", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> BURNING_EMBER_PARTICLE = PARTICLE_TYPES.register(
            "burning_ember", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> BURNING_SPARK_PARTICLE = PARTICLE_TYPES.register(
            "burning_spark", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> BURNING_GRASS_BIT_PARTICLE = PARTICLE_TYPES.register(
            "burning_grass_bit", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> BURNING_BLACK_SMOKE_PARTICLE = PARTICLE_TYPES.register(
            "burning_black_smoke", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> FREEZE_ICE_RING_PARTICLE = PARTICLE_TYPES.register(
            "freeze_ice_ring", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> FREEZE_ICE_CRYSTAL_PARTICLE = PARTICLE_TYPES.register(
            "freeze_ice_crystal", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> FREEZE_FROST_MIST_PARTICLE = PARTICLE_TYPES.register(
            "freeze_frost_mist", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> FREEZE_ICE_SHARD_PARTICLE = PARTICLE_TYPES.register(
            "freeze_ice_shard", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> FREEZE_SNOW_GLINT_PARTICLE = PARTICLE_TYPES.register(
            "freeze_snow_glint", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> SOUL_FLAME_PARTICLE = PARTICLE_TYPES.register(
            "soul_flame", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> DARK_SPARK_PARTICLE = PARTICLE_TYPES.register(
            "dark_spark", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> BLACK_SMOKE_PARTICLE = PARTICLE_TYPES.register(
            "black_smoke", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> SOUL_SHARD_PARTICLE = PARTICLE_TYPES.register(
            "soul_shard", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> SOUL_SPARK_PARTICLE = PARTICLE_TYPES.register(
            "soul_spark", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<SimpleParticleType> SOUL_RING_PARTICLE = PARTICLE_TYPES.register(
            "soul_ring", () -> new SimpleParticleType(true)
    );
    public static final RegistryObject<ParticleType<ReactionTextParticleOptions>> REACTION_TEXT_PARTICLE = PARTICLE_TYPES.register(
            "reaction_text",
            ReactionTextParticleType::new
    );
    public static final RegistryObject<MobEffect> ELEMENTAL_VULNERABLE = MOB_EFFECTS.register(
            "elemental_vulnerable",
            ElementalVulnerableEffect::new
    );
    public static final RegistryObject<MobEffect> IMBALANCED = MOB_EFFECTS.register(
            "imbalanced",
            ImbalancedEffect::new
    );
    public static final RegistryObject<MobEffect> FROZEN = MOB_EFFECTS.register(
            "frozen",
            FrozenEffect::new
    );

    public Elemental_reaction() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        ElementExplosionHandler.setDiffusionParticleEffect(ElementDiffusionParticles::spawn);
        ElementExplosionHandler.setBurningReactionEffect(new BurningReactionEffect() {
            @Override
            public void spawn(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.entity.LivingEntity target,
                              String attachedElement, String incomingElement) {
                BurningParticles.spawn(level, target, attachedElement, incomingElement);
            }
        });
        ElementExplosionHandler.setFreezeParticleEffect(new FreezeParticleEffect() {
            @Override
            public void spawn(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.entity.LivingEntity target,
                              String attachedElement, String incomingElement) {
                FreezeParticles.spawn(level, target, attachedElement, incomingElement);
            }

            @Override
            public void tick(net.minecraft.server.level.ServerLevel level,
                             net.minecraft.world.entity.LivingEntity target) {
                FreezeParticles.tick(level, target);
            }
        });
        ElementExplosionHandler.setTurbulenceParticleEffect(new TurbulenceParticleEffect() {
            @Override
            public void spawn(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.entity.LivingEntity target,
                              String attachedElement, String incomingElement) {
                TurbulenceParticles.spawn(level, target, attachedElement, incomingElement);
            }

            @Override
            public void tick(net.minecraft.server.level.ServerLevel level,
                             net.minecraft.world.entity.LivingEntity target) {
                TurbulenceParticles.tick(level, target);
            }
        });
        ElementExplosionHandler.setMudflowParticleEffect(new MudflowParticleEffect() {
            @Override
            public void spawn(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.entity.LivingEntity target,
                              String attachedElement, String incomingElement) {
                MudflowParticles.spawn(level, target, attachedElement, incomingElement);
            }

            @Override
            public void tick(net.minecraft.server.level.ServerLevel level,
                             net.minecraft.world.entity.LivingEntity target) {
                MudflowParticles.tick(level, target);
            }

            @Override
            public void damageTick(net.minecraft.server.level.ServerLevel level,
                                   net.minecraft.world.entity.LivingEntity target) {
                MudflowParticles.damageTick(level, target);
            }
        });
        ElementExplosionHandler.setSoulScorchParticleEffect(new SoulScorchParticleEffect() {
            @Override
            public void spawn(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.entity.LivingEntity target,
                              String attachedElement, String incomingElement) {
                SoulScorchParticles.spawn(level, target, attachedElement, incomingElement);
            }

            @Override
            public void tick(net.minecraft.server.level.ServerLevel level,
                             net.minecraft.world.entity.LivingEntity target) {
                SoulScorchParticles.tick(level, target);
            }

            @Override
            public void damageTick(net.minecraft.server.level.ServerLevel level,
                                   net.minecraft.world.entity.LivingEntity target) {
                SoulScorchParticles.damageTick(level, target);
            }

            @Override
            public void conversion(net.minecraft.server.level.ServerLevel level,
                                   net.minecraft.world.entity.LivingEntity target) {
                SoulScorchParticles.conversion(level, target);
            }
        });
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
            FrozenEntityRenderer.registerLayers(event);
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ELEMENT_ATTACHMENT_PARTICLE.get(), org.elemental_reaction.client.particle.ElementAttachmentParticle.Provider::new);
            event.registerSpriteSet(ELEMENT_DIFFUSION_PARTICLE.get(), org.elemental_reaction.client.particle.ElementDiffusionParticle.Provider::new);
            event.registerSpriteSet(MUD_BLOB_PARTICLE.get(), org.elemental_reaction.client.particle.MudflowParticle.MudBlobProvider::new);
            event.registerSpriteSet(MUD_CHUNK_PARTICLE.get(), org.elemental_reaction.client.particle.MudflowParticle.MudChunkProvider::new);
            event.registerSpriteSet(SWAMP_MOTE_PARTICLE.get(), org.elemental_reaction.client.particle.MudflowParticle.SwampMoteProvider::new);
            event.registerSpriteSet(WATER_DROP_PARTICLE.get(), org.elemental_reaction.client.particle.MudflowParticle.WaterDropProvider::new);
            event.registerSpriteSet(MUD_RING_PARTICLE.get(), org.elemental_reaction.client.particle.MudflowParticle.MudRingProvider::new);
            event.registerSpriteSet(TURBULENCE_ARC_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.TurbulenceParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.TurbulenceParticle.Kind.ARC));
            event.registerSpriteSet(TURBULENCE_TIDE_DROP_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.TurbulenceParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.TurbulenceParticle.Kind.TIDE_DROP));
            event.registerSpriteSet(TURBULENCE_WATER_DROP_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.TurbulenceParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.TurbulenceParticle.Kind.WATER_DROP));
            event.registerSpriteSet(TURBULENCE_DARK_SPARK_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.TurbulenceParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.TurbulenceParticle.Kind.DARK_SPARK));
            event.registerSpriteSet(TURBULENCE_BLACK_SMOKE_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.TurbulenceParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.TurbulenceParticle.Kind.SMOKE));
            event.registerSpriteSet(BURNING_FLAME_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.BurningParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.BurningParticle.Kind.FLAME));
            event.registerSpriteSet(BURNING_EMBER_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.BurningParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.BurningParticle.Kind.EMBER));
            event.registerSpriteSet(BURNING_SPARK_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.BurningParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.BurningParticle.Kind.SPARK));
            event.registerSpriteSet(BURNING_GRASS_BIT_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.BurningParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.BurningParticle.Kind.GRASS_BIT));
            event.registerSpriteSet(BURNING_BLACK_SMOKE_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.BurningParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.BurningParticle.Kind.SMOKE));
            event.registerSpriteSet(FREEZE_ICE_RING_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.FreezeParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.FreezeParticle.Kind.ICE_RING));
            event.registerSpriteSet(FREEZE_ICE_CRYSTAL_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.FreezeParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.FreezeParticle.Kind.ICE_CRYSTAL));
            event.registerSpriteSet(FREEZE_FROST_MIST_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.FreezeParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.FreezeParticle.Kind.FROST_MIST));
            event.registerSpriteSet(FREEZE_ICE_SHARD_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.FreezeParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.FreezeParticle.Kind.ICE_SHARD));
            event.registerSpriteSet(FREEZE_SNOW_GLINT_PARTICLE.get(),
                    sprites -> new org.elemental_reaction.client.particle.FreezeParticle.Provider(
                            sprites, org.elemental_reaction.client.particle.FreezeParticle.Kind.SNOW_GLINT));
            event.registerSpriteSet(SOUL_FLAME_PARTICLE.get(), sprites -> new org.elemental_reaction.client.particle.SoulScorchParticle.Provider(sprites, org.elemental_reaction.client.particle.SoulScorchParticle.Kind.FLAME));
            event.registerSpriteSet(DARK_SPARK_PARTICLE.get(), sprites -> new org.elemental_reaction.client.particle.SoulScorchParticle.Provider(sprites, org.elemental_reaction.client.particle.SoulScorchParticle.Kind.DARK_SPARK));
            event.registerSpriteSet(BLACK_SMOKE_PARTICLE.get(), sprites -> new org.elemental_reaction.client.particle.SoulScorchParticle.Provider(sprites, org.elemental_reaction.client.particle.SoulScorchParticle.Kind.SMOKE));
            event.registerSpriteSet(SOUL_SHARD_PARTICLE.get(), sprites -> new org.elemental_reaction.client.particle.SoulScorchParticle.Provider(sprites, org.elemental_reaction.client.particle.SoulScorchParticle.Kind.SHARD));
            event.registerSpriteSet(SOUL_SPARK_PARTICLE.get(), sprites -> new org.elemental_reaction.client.particle.SoulScorchParticle.Provider(sprites, org.elemental_reaction.client.particle.SoulScorchParticle.Kind.SOUL_SPARK));
            event.registerSpriteSet(SOUL_RING_PARTICLE.get(), sprites -> new org.elemental_reaction.client.particle.SoulScorchParticle.Provider(sprites, org.elemental_reaction.client.particle.SoulScorchParticle.Kind.RING));
            event.registerSpecial(
                    REACTION_TEXT_PARTICLE.get(),
                    new org.elemental_reaction.client.particle.ReactionTextParticle.Provider()
            );
        }
    }
}
