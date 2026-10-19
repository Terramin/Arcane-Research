package arcaneresearch.proxies;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.lib.events.KeyHandler;
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    private final ProxyEntities proxyEntities = new ProxyEntities();
    private final ProxyTESR proxyTESR = new ProxyTESR();
    private final ProxyBlock proxyBlock = new ProxyBlock();
    public ClientProxy() {
        super();
    }
    @Override
    public void preInit() {
        super.preInit();
        ProxyBlock.registerDomain();
        initShaders();
    }
    @Override
    public void init() {
        super.init();
        KeyHandler.init();
    }
    public void initShaders() {
    }
    public void registerKeyBindings() {
        KeyHandler.init();
    }
    public ProxyEntities getProxyEntities() {
        return proxyEntities;
    }
    public ProxyTESR getProxyTESR() {
        return proxyTESR;
    }
    public ProxyBlock getProxyBlock() {
        return proxyBlock;
    }
    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
    @Override
    public Level getLevel(ResourceKey<Level> dimension) {
        Level clientLevel = getClientLevel();
        if (clientLevel != null && clientLevel.dimension().equals(dimension)) {
            return clientLevel;
        }
        return super.getLevel(dimension);
    }
    @Override
    public boolean getSingleplayer() {
        return Minecraft.getInstance().isSingleplayer();
    }
    @Override
    public boolean isShiftKeyDown() {
        return Screen.hasShiftDown();
    }
    public void registerModel(BlockItem itemBlock) {
    }
    public void registerModel(ItemStack stack) {
    }
    @Override
    public arcaneresearch.api.golems.parts.PartModel createGolemPartModel(String kind,
                                                                      net.minecraft.resources.ResourceLocation objModel,
                                                                      net.minecraft.resources.ResourceLocation objTexture,
                                                                      arcaneresearch.api.golems.parts.PartModel.EnumAttachPoint attachPoint) {
        return switch (kind) {
            case "CLAWS" ->
                    new arcaneresearch.client.renderers.entity.golem.PartModelClaws(objModel, objTexture, attachPoint);
            case "BREAKERS" ->
                    new arcaneresearch.client.renderers.entity.golem.PartModelBreakers(objModel, objTexture, attachPoint);
            case "DARTS" ->
                    new arcaneresearch.client.renderers.entity.golem.PartModelDarts(objModel, objTexture, attachPoint);
            case "WHEEL" ->
                    new arcaneresearch.client.renderers.entity.golem.PartModelWheel(objModel, objTexture, attachPoint);
            case "HAULER" ->
                    new arcaneresearch.client.renderers.entity.golem.PartModelHauler(objModel, objTexture, attachPoint);
            default -> new arcaneresearch.api.golems.parts.PartModel(objModel, objTexture, attachPoint);
        };
    }
}
