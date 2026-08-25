![Recipe Book Access Banner](https://cdn.modrinth.com/data/cached_images/6a9610664160b3d32157edee9c484d5c04077bc1.png)

Recipe Book Access is a simple Fabric API designed to intuitively add support for external inventory access in any crafting screen. \
It redirects the recipe book functionality to check and fill from a customisable list of inventories, instead of just the player's.

### What does this mean?
See for yourself! 

![Recipe Book Access Demonstration](https://raw.githubusercontent.com/Jomlom/Recipe-Book-Access/refs/heads/main/demo.gif) \
This is an EXAMPLE of a modded crafting table uses a custom list of inventories via this API, in this specific case it uses all inventories from nearby chests.

### This mod _alone_ will not achieve what is shown above - If you are looking for a mod that implements this for the crafting table, you probably want the [Nearby Crafting](https://modrinth.com/mod/nearby-crafting) mod.

## Quickstart Guide
**For a more detailed how-to-use guide, including how to setup this API in your project's environment, please see the readme on Github for this project [here](https://github.com/Jomlom/Recipe-Book-Access).**

This API provides a simple interface called `RecipeBookInventoryProvider`, located in `com.jomlom.recipebookaccess.api`. 

This interface must be implemented to your screen handler class, only one method needs overriding (`getInventoriesForAutofill()`) for the API's full functionality.

You must implement this method to return a list of inventories which you want the recipe book to access and craft from, this won't include the player's inventory by default.

_Code Example:_
```java
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider

public class YourCraftingScreenHandler extends AbstractRecipeScreenHandler implements RecipeBookInventoryProvider {
    // your existing code...

    @Override
    public List<Inventory> getInventoriesForAutofill() {
        return yourInventoriesList;
    }
}
```

**Assumptions made by this API:**
- Your screen handler class extends 'AbstractRecipeScreenHandler', or any of its subclasses
- Your corresponding screen extends 'RecipeBookScreen', in order to utilize the minecraft recipe book

(_I have not tested outside these assumptions_)


## FAQs
- **Q**: I think I found an issue... \
  **A**: Feel free to create an issue on [Github](https://github.com/Jomlom/Nearby-Crafting/issues) or reach out directly by email ```contact@jontyali.com```.
 
- **Q**: Will you release support for _x_ ? \
  **A**: Again, feel free to reach out with reqeusts to support certain versions or other mods. I plan to explore support for NeoForge soon.

- **Q**: Does my `getInventoriesForAutofill()` implementation need to work from the client? \
  **A**: Nope! `getInventoriesForAutofill()` is only used from the server side screen handler, and the API will keep the client up to date on the results for you automatically!
