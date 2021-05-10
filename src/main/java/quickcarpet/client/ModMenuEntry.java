package quickcarpet.client;

@SuppressWarnings("deprecation")
public class ModMenuEntry implements io.github.prospector.modmenu.api.ModMenuApi {
    @Override
    public io.github.prospector.modmenu.api.ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigsGui::new;
    }
}
