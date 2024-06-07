package fuzs.pixelshot.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class DirectionHelper {
    static final Map<Direction, Vector3f> VALUES = Arrays.stream(Direction.values())
            .collect(Maps.<Direction, Direction, Vector3f>toImmutableEnumMap(Function.identity(),
                    DirectionHelper::getAnglesFromRotation
            ));
    static final Map<Direction, Vector3f> NEW_VALUES;

    static {
        // DOWN 180 0 0
        // UP 0 0 0
        // NORTH 90 0 180
        // SOUTH 90 0 0
        // WEST 90 0 90
        // EAST 90 0 -90
        ImmutableMap.Builder<Direction, Vector3f> builder = ImmutableMap.builder();
        // +z
        builder.put(Direction.DOWN, new Vector3f(90.0F, 0.0F, 0.0F));
        // -z
        builder.put(Direction.UP, new Vector3f(-90.0F, 0.0F, 0.0F));
        // -x
        builder.put(Direction.NORTH, new Vector3f(0.0F, 0.0F, 180.0F));
        // +x
        builder.put(Direction.SOUTH, new Vector3f(0.0F, 0.0F, 0.0F));
        // +y
        builder.put(Direction.WEST, new Vector3f(0.0F, 0.0F, 90.0F));
        // -y
        builder.put(Direction.EAST, new Vector3f(0.0F, 0.0F, -90.0F));
        NEW_VALUES = Maps.immutableEnumMap(builder.build());
    }

    static Vector3f getAnglesFromRotation(Direction direction) {
        return direction.getRotation().getEulerAnglesXYZ(new Vector3f()).mul(180.0F / Mth.PI).sub(90.0F, 0.0F, 0.0F).round();
    }

    public static Direction cycleForward(Direction direction) {
        return Direction.from3DDataValue(direction.get3DDataValue() + 1);
    }

    public static Direction cycleBackward(Direction direction) {
        return Direction.from3DDataValue(direction.get3DDataValue() + 5);
    }

    public static Vector3f getAngles(Direction direction) {
        return VALUES.get(direction);
    }

    public static Vector3f cycle(float xRot, float yRot, boolean forward) {
        Direction direction = getNearest(xRot, yRot);
        Vector3f vector3f = getAngles(direction);
        if (vector3f.x() == xRot && vector3f.z() == yRot) {
            direction = forward ? cycleForward(direction) : cycleBackward(direction);
        }

        return getAngles(direction);
    }

    public static Vector3f getDirectionVector(float pitch, float yaw) {
        float x = Mth.cos(yaw) * Mth.cos(pitch);
        float y = Mth.sin(yaw) * Mth.cos(pitch);
        float z = Mth.sin(pitch);
        return new Vector3f(-y, -z, x);
    }

    public static Direction getNearest(float xRot, float yRot) {
        Direction direction = Direction.NORTH;
        float f = Float.MAX_VALUE;
        for (Map.Entry<Direction, Vector3f> entry : NEW_VALUES.entrySet()) {
            Vector3f vector3f = entry.getValue();
            float value = Math.abs(Mth.wrapDegrees(vector3f.x() - xRot)) + Math.abs(Mth.wrapDegrees(vector3f.z() - yRot));
            if (value < f) {
                f = value;
                direction = entry.getKey();
            }
        }

        return direction;
    }
}
