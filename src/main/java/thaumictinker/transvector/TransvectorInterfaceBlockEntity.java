package thaumictinker.transvector;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aspects.IEssentiaTransport;

 


 
public class TransvectorInterfaceBlockEntity extends TransvectorBlockEntity implements IAspectContainer, IEssentiaTransport {

    public static final int DISTANCE = 4;

    public TransvectorInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TransvectorFeature.INTERFACE_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransvectorInterfaceBlockEntity entity) {
        if (level.getGameTime() % 100 == 0) {
            level.updateNeighborsAt(pos, state.getBlock());
        }
    }

    @Nullable
    public static BlockEntity proxyTarget(TransvectorInterfaceBlockEntity entity) {
        BlockEntity target = entity.getTarget();
        return target instanceof TransvectorInterfaceBlockEntity ? null : target;
    }

    @Override
    public int maxDistance() {
        return DISTANCE;
    }

    @Nullable
    private IAspectContainer container() {
        return proxyTarget(this) instanceof IAspectContainer container ? container : null;
    }

    @Nullable
    private IEssentiaTransport transport() {
        return proxyTarget(this) instanceof IEssentiaTransport transport ? transport : null;
    }

    

    @Override
    public AspectList getAspects() {
        IAspectContainer container = container();
        return container == null ? new AspectList() : container.getAspects();
    }

    @Override
    public void setAspects(AspectList aspects) {
        IAspectContainer container = container();
        if (container != null) {
            container.setAspects(aspects);
        }
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        IAspectContainer container = container();
        return container != null && container.doesContainerAccept(aspect);
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        IAspectContainer container = container();
        return container == null ? amount : container.addToContainer(aspect, amount);
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        IAspectContainer container = container();
        return container != null && container.takeFromContainer(aspect, amount);
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        IAspectContainer container = container();
        if (container == null) {
            return false;
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (container.containerContains(aspect) < aspects.getAmount(aspect)) {
                return false;
            }
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (!container.takeFromContainer(aspect, aspects.getAmount(aspect))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        IAspectContainer container = container();
        return container != null && container.doesContainerContainAmount(aspect, amount);
    }

    @Override
    public boolean doesContainerContain(AspectList aspects) {
        IAspectContainer container = container();
        if (container == null) {
            return false;
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (container.containerContains(aspect) < aspects.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int containerContains(Aspect aspect) {
        IAspectContainer container = container();
        return container == null ? 0 : container.containerContains(aspect);
    }

    

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        IEssentiaTransport transport = transport();
        return transport != null && transport.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        IEssentiaTransport transport = transport();
        return transport != null && transport.canOutputTo(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        IEssentiaTransport transport = transport();
        if (transport != null) {
            transport.setSuction(aspect, amount);
        }
    }

    @Override
    @Nullable
    public Aspect getSuctionType(Direction face) {
        IEssentiaTransport transport = transport();
        return transport == null ? null : transport.getSuctionType(face);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        IEssentiaTransport transport = transport();
        return transport == null ? 0 : transport.getSuctionAmount(face);
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        IEssentiaTransport transport = transport();
        return transport == null ? 0 : transport.takeEssentia(aspect, amount, face);
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        IEssentiaTransport transport = transport();
        return transport == null ? 0 : transport.addEssentia(aspect, amount, face);
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(Direction face) {
        IEssentiaTransport transport = transport();
        return transport == null ? null : transport.getEssentiaType(face);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        IEssentiaTransport transport = transport();
        return transport == null ? 0 : transport.getEssentiaAmount(face);
    }

    @Override
    public int getMinimumSuction() {
        IEssentiaTransport transport = transport();
        return transport == null ? 0 : transport.getMinimumSuction();
    }
}
