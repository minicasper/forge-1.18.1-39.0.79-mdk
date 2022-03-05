package com.example.examplemod;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.obj.OBJModel.ModelSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("examplemod")
public class ExampleMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MODID = "examplemod";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final BlockBehaviour.Properties BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.STONE).strength(2f).requiresCorrectToolForDrops();
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab( new CreativeModeTab("tutorialv3") {

		@Override
		public ItemStack makeIcon() {
			// TODO Auto-generated method stub
			return new ItemStack(Blocks.IRON_ORE);
		}
    });

    public static final RegistryObject<Block> MYSTERIOUS_ORE_OVERWORLD = BLOCKS.register("mysterious_ore_overworld", () -> new Block(BLOCK_PROPERTIES));
    public static final RegistryObject<Item> MYSTERIOUS_ORE_OVERWORLD_ITEM = fromBlock(MYSTERIOUS_ORE_OVERWORLD);
    public static final RegistryObject<Block> MYSTERIOUS_ORE_NETHER = BLOCKS.register("mysterious_ore_nether", () -> new Block(BLOCK_PROPERTIES));
    public static final RegistryObject<Item> MYSTERIOUS_ORE_NETHER_ITEM = fromBlock(MYSTERIOUS_ORE_NETHER);
    public static final RegistryObject<Block> MYSTERIOUS_ORE_END = BLOCKS.register("mysterious_ore_end", () -> new Block(BLOCK_PROPERTIES));
    public static final RegistryObject<Item> MYSTERIOUS_ORE_END_ITEM = fromBlock(MYSTERIOUS_ORE_END);
    public static final RegistryObject<Block> MYSTERIOUS_ORE_DEEPSLATE = BLOCKS.register("mysterious_ore_deepslate", () -> new Block(BLOCK_PROPERTIES));
    public static final RegistryObject<Item> MYSTERIOUS_ORE_DEEPSLATE_ITEM = fromBlock(MYSTERIOUS_ORE_DEEPSLATE);

    // Conveniance function: Take a RegistryObject<Block> and make a corresponding RegistryObject<Item> from it
    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    } 
    
    public ExampleMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        
        BLOCKS.register(bus);
        ITEMS.register(bus);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        LOGGER.info("ACACIA_DOOR >> {}", Blocks.ACACIA_DOOR.getRegistryName());
        
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.messageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    //static Block testBlock;
    
    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
            
			/*
			 * testBlock = new Block(Properties.of(Material.GLASS,
			 * DyeColor.ORANGE).strength(10).sound(SoundType.GLASS)).setRegistryName(MODID,
			 * "testBlock1"); blockRegistryEvent.getRegistry().register(testBlock);
			 */
        }
        
		/*
		 * @SubscribeEvent public static void onItemsRegistry(final
		 * RegistryEvent.Register<Item> event ) {
		 * event.getRegistry().register(testBlock.asItem()); }
		 */
    }
    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DataGenerators {

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            DataGenerator generator = event.getGenerator();
            if (event.includeServer()) {
                generator.addProvider(new TutRecipes(generator));
                generator.addProvider(new TutLootTables(generator));
                TutBlockTags blockTags = new TutBlockTags(generator, event.getExistingFileHelper());
                generator.addProvider(blockTags);
                generator.addProvider(new TutItemTags(generator, blockTags, event.getExistingFileHelper()));
            }
            if (event.includeClient()) {
                generator.addProvider(new TutBlockStates(generator, event.getExistingFileHelper()));
                generator.addProvider(new TutItemModels(generator, event.getExistingFileHelper()));
                generator.addProvider(new TutLanguageProvider(generator, "en_us"));
            }
        }
    }
    
    public static class TutBlockStates extends BlockStateProvider {

        public TutBlockStates(DataGenerator gen, ExistingFileHelper helper) {
            super(gen, MODID, helper);
        }

        @Override
        protected void registerStatesAndModels() {
            simpleBlock(MYSTERIOUS_ORE_OVERWORLD.get());
            simpleBlock(MYSTERIOUS_ORE_NETHER.get());
            simpleBlock(MYSTERIOUS_ORE_END.get());
            simpleBlock(MYSTERIOUS_ORE_DEEPSLATE.get());
        }
    }


    public static class TutItemModels extends ItemModelProvider {

        public TutItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, MODID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            withExistingParent(MYSTERIOUS_ORE_OVERWORLD_ITEM.get().getRegistryName().getPath(), modLoc("block/mysterious_ore_overworld"));
            withExistingParent(MYSTERIOUS_ORE_NETHER_ITEM.get().getRegistryName().getPath(), modLoc("block/mysterious_ore_nether"));
            withExistingParent(MYSTERIOUS_ORE_END_ITEM.get().getRegistryName().getPath(), modLoc("block/mysterious_ore_end"));
            withExistingParent(MYSTERIOUS_ORE_DEEPSLATE_ITEM.get().getRegistryName().getPath(), modLoc("block/mysterious_ore_deepslate"));
        }
    }

    public static class TutBlockTags extends BlockTagsProvider {

        public TutBlockTags(DataGenerator generator, ExistingFileHelper helper) {
            super(generator, MODID, helper);
        }

        @Override
        protected void addTags() {
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(MYSTERIOUS_ORE_OVERWORLD.get())
                    .add(MYSTERIOUS_ORE_NETHER.get())
                    .add(MYSTERIOUS_ORE_END.get())
                    .add(MYSTERIOUS_ORE_DEEPSLATE.get());
            tag(BlockTags.NEEDS_IRON_TOOL)
                    .add(MYSTERIOUS_ORE_OVERWORLD.get())
                    .add(MYSTERIOUS_ORE_NETHER.get())
                    .add(MYSTERIOUS_ORE_END.get())
                    .add(MYSTERIOUS_ORE_DEEPSLATE.get());
            tag(Tags.Blocks.ORES)
                    .add(MYSTERIOUS_ORE_OVERWORLD.get())
                    .add(MYSTERIOUS_ORE_NETHER.get())
                    .add(MYSTERIOUS_ORE_END.get())
                    .add(MYSTERIOUS_ORE_DEEPSLATE.get());
        }

        @Override
        public String getName() {
            return "Tutorial Tags";
        }
    }

    public static class TutItemTags extends ItemTagsProvider {

        public TutItemTags(DataGenerator generator, BlockTagsProvider blockTags, ExistingFileHelper helper) {
            super(generator, blockTags, MODID, helper);
        }

        @Override
        protected void addTags() {
            tag(Tags.Items.ORES)
                    .add(MYSTERIOUS_ORE_OVERWORLD_ITEM.get())
                    .add(MYSTERIOUS_ORE_NETHER_ITEM.get())
                    .add(MYSTERIOUS_ORE_END_ITEM.get())
                    .add(MYSTERIOUS_ORE_DEEPSLATE_ITEM.get());
        }

        @Override
        public String getName() {
            return "Tutorial Tags";
        }
    }

    public static class TutLanguageProvider extends LanguageProvider {

        public TutLanguageProvider(DataGenerator gen, String locale) {
            super(gen, MODID, locale);
        }

        @Override
        protected void addTranslations() {
            add("itemGroup." + "tutorialv3", "Tutorial");
            add(MYSTERIOUS_ORE_OVERWORLD.get(), "Mysterious ore");
            add(MYSTERIOUS_ORE_NETHER.get(), "Mysterious ore");
            add(MYSTERIOUS_ORE_END.get(), "Mysterious ore");
            add(MYSTERIOUS_ORE_DEEPSLATE.get(), "Mysterious ore");
        }
    }

    public static class TutRecipes extends RecipeProvider {

        public TutRecipes(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        }
    }

    public static class TutLootTables extends BaseLootTableProvider {

        public TutLootTables(DataGenerator dataGeneratorIn) {
            super(dataGeneratorIn);
        }

        @Override
        protected void addTables() {
        }
    }
    
    public static abstract class BaseLootTableProvider extends LootTableProvider {

        private static final Logger LOGGER = LogManager.getLogger();
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        protected final Map<Block, LootTable.Builder> lootTables = new HashMap<>();
        private final DataGenerator generator;

        public BaseLootTableProvider(DataGenerator dataGeneratorIn) {
            super(dataGeneratorIn);
            this.generator = dataGeneratorIn;
        }

        protected abstract void addTables();

        protected LootTable.Builder createStandardTable(String name, Block block, BlockEntityType<?> type) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(name)
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(block)
                            .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                    .copy("Info", "BlockEntityTag.Info", CopyNbtFunction.MergeStrategy.REPLACE)
                                    .copy("Inventory", "BlockEntityTag.Inventory", CopyNbtFunction.MergeStrategy.REPLACE)
                                    .copy("Energy", "BlockEntityTag.Energy", CopyNbtFunction.MergeStrategy.REPLACE))
                            .apply(SetContainerContents.setContents(type)
                                    .withEntry(DynamicLoot.dynamicEntry(new ResourceLocation("minecraft", "contents"))))
                    );
            return LootTable.lootTable().withPool(builder);
        }

        protected LootTable.Builder createSimpleTable(String name, Block block) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(name)
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(block));
            return LootTable.lootTable().withPool(builder);
        }

        protected LootTable.Builder createSilkTouchTable(String name, Block block, Item lootItem, float min, float max) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(name)
                    .setRolls(ConstantValue.exactly(1))
                    .add(AlternativesEntry.alternatives(
                                    LootItem.lootTableItem(block)
                                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                                    .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))),
                                    LootItem.lootTableItem(lootItem)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 1))
                                            .apply(ApplyExplosionDecay.explosionDecay())
                            )
                    );
            return LootTable.lootTable().withPool(builder);
        }


        @Override
        public void run(HashCache cache) {
            addTables();

            Map<ResourceLocation, LootTable> tables = new HashMap<>();
            for (Map.Entry<Block, LootTable.Builder> entry : lootTables.entrySet()) {
                tables.put(entry.getKey().getLootTable(), entry.getValue().setParamSet(LootContextParamSets.BLOCK).build());
            }
            writeTables(cache, tables);
        }

        private void writeTables(HashCache cache, Map<ResourceLocation, LootTable> tables) {
            Path outputFolder = this.generator.getOutputFolder();
            tables.forEach((key, lootTable) -> {
                Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
                try {
                    DataProvider.save(GSON, cache, LootTables.serialize(lootTable), path);
                } catch (IOException e) {
                    LOGGER.error("Couldn't write loot table {}", path, e);
                }
            });
        }

        @Override
        public String getName() {
            return "MyTutorial LootTables";
        }
    }
}
