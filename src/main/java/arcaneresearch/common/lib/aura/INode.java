package arcaneresearch.common.lib.aura;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;


public interface INode extends IAspectContainer {
    AspectList getAspectsBase();

    NodeType getNodeType();

    void setNodeType(NodeType nodeType);

    NodeModifier getNodeModifier();

    void setNodeModifier(NodeModifier nodeModifier);

    int getNodeVisBase(Aspect aspect);

    void setNodeVisBase(Aspect aspect, short nodeVisBase);

    String getId();

    void setId(String id);
}
