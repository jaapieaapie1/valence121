package rs.valence.extractor;

import java.io.Serializable;
import java.util.Comparator;
import net.minecraft.resources.ResourceKey;

public class RegistryKeyComparator
    implements Comparator<ResourceKey<?>>, Serializable {

    public RegistryKeyComparator() {}

    @Override
    public int compare(ResourceKey<?> o1, ResourceKey<?> o2) {
        var c1 = o1.registry().compareTo(o2.registry());

        if (0 != c1) {
            return c1;
        }

        return o1.identifier().compareTo(o2.identifier());
    }
}
