package dev.mayaqq.cynosure.tooltips

import dev.mayaqq.cynosure.helpers.McFont
import dev.mayaqq.cynosure.injection.client.javaLocale
import dev.mayaqq.cynosure.items.extensions.CustomTooltip
import dev.mayaqq.cynosure.utils.colors.Color
import dev.mayaqq.cynosure.utils.colors.DarkGray
import dev.mayaqq.cynosure.utils.colors.LightGray
import dev.mayaqq.cynosure.utils.language.words
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

public class DescriptionTooltip(
    private val theme: Theme
) : CustomTooltip {

    private val lines: MutableList<Component> = mutableListOf()
    private val hiddenLines: MutableList<Component> = mutableListOf()
    private var cachedLanguage: String? = null

    override fun MutableList<Component>.modifyTooltip(stack: ItemStack, player: Player?, flags: TooltipFlag) {
        if (checkLanguage()) {
            rebuild(stack.item)
        }
        addAll(1, lines)
    }

    private fun rebuild(stack: Item) {
        lines.clear()
        val key = stack.descriptionId + ".tooltip"
        // TODO: Split and all kinds of other stuff
        lines.add(Component.empty())
        lines.addAll(Component.translatable(key).format(theme))
    }

    private fun checkLanguage(): Boolean {
        val selected = Minecraft.getInstance().languageManager.selected
        if (selected != cachedLanguage) {
            cachedLanguage = selected
            return true
        }
        return false
    }

    public data class Theme(
        val primaryColor: Color,
        val secodaryColor: Color,
        val highlightColor: Color
    )

    public object Themes {
        @JvmField
        public val Default: Theme = Theme(DarkGray, LightGray, Color(0x84597Eu))
    }

    private fun Component.format(theme: Theme): List<Component> {
        val lines = buildList {
            var totalWidth = 0
            var currentLine = ""
            string.words(Minecraft.getInstance().javaLocale).forEach { word ->
                val width = McFont.width(word.replace("_", ""))
                if (totalWidth + width > MAX_LINE_WIDTH) {
                    if (totalWidth > 0) {
                        add(currentLine)
                        currentLine = ""
                        totalWidth = 0
                    } else {
                        add(word)
                        return@forEach
                    }
                }
                totalWidth += width
                currentLine += word
            }
            if (totalWidth > 0) add(currentLine)
        }

        var highlighted = false
        return lines.map { string ->
            val final = Component.empty()
            string.split("_").forEach { part ->
                final.append(Component.literal(part).withStyle(
                    Style.EMPTY.withColor(
                        if (highlighted) theme.highlightColor.toInt() else theme.secodaryColor.toInt()
                    )))
                highlighted = !highlighted
            }
            final
        }
    }

    public companion object {
        public const val MAX_LINE_WIDTH: Int = 200
    }
}