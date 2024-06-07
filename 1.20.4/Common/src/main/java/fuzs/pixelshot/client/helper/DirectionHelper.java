package fuzs.pixelshot.client.helper;

import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class DirectionHelper {
    static final Map<Direction, Vector3f> VALUES = Arrays.stream(Direction.values())
            .collect(Maps.<Direction, Direction, Vector3f>toImmutableEnumMap(Function.identity(),
                    DirectionHelper::getAnglesFromRotation
            ));

    static Vector3f getAnglesFromRotation(Direction direction) {
        // angles we get from these that we use for setting up our pitch & yaw
        // these match the player facing values on the debug screen
        // DOWN 90 0 0
        // UP -90 0 0
        // NORTH 0 0 180
        // SOUTH 0 0 0
        // WEST 0 0 90
        // EAST 0 0 -90
        return direction.getRotation()
                .getEulerAnglesXYZ(new Vector3f())
                .mul(Mth.RAD_TO_DEG)
                .sub(90.0F, 0.0F, 0.0F)
                .round();
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
        Vector3f angles = getAngles(direction);
        if (angles.x() == xRot && angles.z() == yRot) {
            direction = forward ? cycleForward(direction) : cycleBackward(direction);
            return getAngles(direction);
        } else {
            return angles;
        }
    }

    public static Vector3f getDirectionVector(float pitch, float yaw) {
        // from https://stackoverflow.com/a/1568687
        float x = Mth.cos(yaw) * Mth.cos(pitch);
        float y = Mth.sin(yaw) * Mth.cos(pitch);
        float z = Mth.sin(pitch);
        return new Vector3f(-y, -z, x);
    }

    public static Direction getNearest(float xRot, float yRot) {
        Vector3f vector3f = DirectionHelper.getDirectionVector(xRot * Mth.DEG_TO_RAD, yRot * Mth.DEG_TO_RAD);
        int maxComponent = vector3f.absolute(new Vector3f()).maxComponent();
        Vector3i vector3i = new Vector3i().setComponent(maxComponent, (int) Math.signum(vector3f.get(maxComponent)));
        return Direction.fromDelta(vector3i.x(), vector3i.y(), vector3i.z());
    }
}
