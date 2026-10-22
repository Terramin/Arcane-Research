package arcaneresearch.client.lib.obj;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MeshModel {
    public List<Vector3f> positions;
    public List<Vector3f> normals;
    public List<Vector2f> texCoords;
    public List<MeshPart> parts;

    public MeshModel() {
        parts = new ArrayList<MeshPart>();
    }

    private static Vector3f faceNormal(Vector3f a, Vector3f b, Vector3f c) {
        float abx = b.x - a.x;
        float aby = b.y - a.y;
        float abz = b.z - a.z;
        float acx = c.x - a.x;
        float acy = c.y - a.y;
        float acz = c.z - a.z;
        float nx = aby * acz - abz * acy;
        float ny = abz * acx - abx * acz;
        float nz = abx * acy - aby * acx;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1.0e-6f) {
            return new Vector3f(0.0f, 1.0f, 0.0f);
        }
        return new Vector3f(nx / length, ny / length, nz / length);
    }

    private static Direction getFacingFromPositions(Vector3f a, Vector3f b, Vector3f c) {
        Vector3f normal = faceNormal(a, b, c);
        float ax = Math.abs(normal.x);
        float ay = Math.abs(normal.y);
        float az = Math.abs(normal.z);
        if (ax >= ay && ax >= az) {
            return normal.x > 0.0f ? Direction.EAST : Direction.WEST;
        }
        if (ay >= ax && ay >= az) {
            return normal.y > 0.0f ? Direction.UP : Direction.DOWN;
        }
        return normal.z > 0.0f ? Direction.SOUTH : Direction.NORTH;
    }

    public MeshModel clone() {
        MeshModel mm = new MeshModel();
        mm.parts = new ArrayList<MeshPart>();
        for (MeshPart mp : parts) {
            mm.parts.add(mp);
        }
        if (positions != null) {
            mm.positions = new ArrayList<Vector3f>();
            for (Vector3f mp2 : positions) {
                mm.positions.add(new Vector3f(mp2));
            }
        }
        if (normals != null) {
            mm.normals = new ArrayList<Vector3f>();
            for (Vector3f mp2 : normals) {
                mm.normals.add(new Vector3f(mp2));
            }
        }
        if (texCoords != null) {
            mm.texCoords = new ArrayList<Vector2f>();
            for (Vector2f mp3 : texCoords) {
                mm.texCoords.add(new Vector2f(mp3));
            }
        }
        return mm;
    }

    public void rotate(double angle, Vector3f axis, Vector3f offset) {
        double halfAngle = angle * 0.5;
        double s = Math.cos(halfAngle);
        double sin = Math.sin(halfAngle);
        double qx = axis.x * sin;
        double qy = axis.y * sin;
        double qz = axis.z * sin;
        List<Vector3f> p = new ArrayList<Vector3f>();
        for (Vector3f v : positions) {
            double vx = v.x;
            double vy = v.y;
            double vz = v.z;
            double d = -qx * vx - qy * vy - qz * vz;
            double d2 = s * vx + qy * vz - qz * vy;
            double d3 = s * vy - qx * vz + qz * vx;
            double d4 = s * vz + qx * vy - qy * vx;
            float rx = (float) (d2 * s - d * qx - d3 * qz + d4 * qy);
            float ry = (float) (d3 * s - d * qy + d2 * qz - d4 * qx);
            float rz = (float) (d4 * s - d * qz - d2 * qy + d3 * qx);
            p.add(new Vector3f(rx + offset.x, ry + offset.y, rz + offset.z));
        }
        positions = p;
    }

    public void addPosition(float x, float y, float z) {
        if (positions == null) {
            positions = new ArrayList<Vector3f>();
        }
        positions.add(new Vector3f(x, y, z));
    }

    public void addNormal(float x, float y, float z) {
        if (normals == null) {
            normals = new ArrayList<Vector3f>();
        }
        normals.add(new Vector3f(x, y, z));
    }

    public void addTexCoords(float x, float y) {
        if (texCoords == null) {
            texCoords = new ArrayList<Vector2f>();
        }
        texCoords.add(new Vector2f(x, y));
    }

    public void addPart(MeshPart part) {
        parts.add(part);
    }

    public void addPart(MeshPart part, int ti) {
        parts.add(new MeshPart(part, ti));
    }

    private int getColorValue(Vector3f color) {
        int r = (int) color.x;
        int g = (int) color.y;
        int b = (int) color.z;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public List<BakedQuad> bakeModel(TextureAtlasSprite sprite) {
        List<BakedQuad> bakeList = new ArrayList<BakedQuad>();
        BakedQuad.MaterialInfo base = defaultMaterialInfo(sprite);
        for (int j = 0; j < parts.size(); ++j) {
            MeshPart part = parts.get(j);
            for (int i = 0; i < part.indices.size(); i += 4) {
                BakedQuad quad = bakeQuad(part, i, sprite, base);
                if (quad != null) {
                    bakeList.add(quad);
                }
            }
        }
        return bakeList;
    }

    public List<BakedQuad> bakeModel(TextureAtlasSprite sprite, BakedQuad.MaterialInfo baseInfo) {
        List<BakedQuad> bakeList = new ArrayList<BakedQuad>();
        for (int j = 0; j < parts.size(); ++j) {
            MeshPart part = parts.get(j);
            for (int i = 0; i < part.indices.size(); i += 4) {
                BakedQuad quad = bakeQuad(part, i, sprite, baseInfo);
                if (quad != null) {
                    bakeList.add(quad);
                }
            }
        }
        return bakeList;
    }

    private static BakedQuad.MaterialInfo defaultMaterialInfo(TextureAtlasSprite sprite) {
        return new BakedQuad.MaterialInfo(sprite, ChunkSectionLayer.CUTOUT,
                Sheets.cutoutBlockItemSheet(), Sheets.cutoutBlockItemGlintSheet(),
                Sheets.cutoutBlockItemGlintSpecialSheet(), -1, null, 0, true);
    }

    private static BakedQuad.MaterialInfo withTint(BakedQuad.MaterialInfo base,
                                                   TextureAtlasSprite sprite, int tint) {
        return new BakedQuad.MaterialInfo(
                base.sprite() != null ? base.sprite() : sprite,
                base.layer(), base.itemRenderType(), base.itemGlintRenderType(),
                base.itemGlintSpecialRenderType(), tint, base.shadeDirectionOverride(),
                base.lightEmission(), base.ambientOcclusion());
    }

    private BakedQuad bakeQuad(MeshPart part, int startIndex, TextureAtlasSprite sprite,
                               BakedQuad.MaterialInfo baseInfo) {
        Vector3f[] cornerPositions = new Vector3f[4];
        Vector2f[] cornerTexCoords = new Vector2f[4];
        for (int i = 0; i < 4; ++i) {
            Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
            Vector2f texCoord = new Vector2f(0.0f, 0.0f);
            int p = 0;
            int[] indices = part.indices.get(startIndex + i);
            if (positions != null) {
                position = positions.get(indices[p++]);
            }
            if (normals != null) {
                ++p;
            }
            if (texCoords != null) {
                texCoord = texCoords.get(indices[p++]);
            }
            cornerPositions[i] = position;
            cornerTexCoords[i] = texCoord;
        }
        int tint = (part.name != null && part.name.contains("focus")) ? 1 : part.tintIndex;
        Direction facing = getFacingFromPositions(cornerPositions[0], cornerPositions[1], cornerPositions[2]);
        
        
        
        Vector3f normal = faceNormal(cornerPositions[0], cornerPositions[1], cornerPositions[2]);
        int packedNormal = BakedNormals.pack(normal);
        Vector3f p0 = new Vector3f(cornerPositions[0]);
        Vector3f p1 = new Vector3f(cornerPositions[1]);
        Vector3f p2 = new Vector3f(cornerPositions[2]);
        Vector3f p3 = new Vector3f(cornerPositions[3]);
        long uv0 = packUv(sprite, cornerTexCoords[0]);
        long uv1 = packUv(sprite, cornerTexCoords[1]);
        long uv2 = packUv(sprite, cornerTexCoords[2]);
        long uv3 = packUv(sprite, cornerTexCoords[3]);
        return new BakedQuad(p0, p1, p2, p3, uv0, uv1, uv2, uv3, facing,
                withTint(baseInfo, sprite, tint),
                BakedNormals.of(packedNormal, packedNormal, packedNormal, packedNormal),
                net.neoforged.neoforge.client.model.quad.BakedColors.DEFAULT);
    }

    private static long packUv(TextureAtlasSprite sprite, Vector2f texCoord) {
        if (sprite != null) {
            return UVPair.pack(sprite.getU(texCoord.x), sprite.getV(texCoord.y));
        }
        return UVPair.pack(texCoord.x, texCoord.y);
    }
}
