package p455w0rd.wit.client.gui;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;

import appeng.api.AEApi;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.*;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.api.client.gui.GuiWT;
import p455w0rd.ae2wtlib.api.client.gui.widgets.*;
import p455w0rd.ae2wtlib.api.container.ContainerWT;
import p455w0rd.wit.container.ContainerWIT;
import p455w0rd.wit.init.ModGlobals;
import p455w0rd.wit.init.ModNetworking;
import p455w0rd.wit.sync.packets.PacketInventoryAction;

public class GuiWIT extends GuiWT {

	private static final int LINES_ON_PAGE = 5;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(ModGlobals.MODID, "textures/gui/interfaceterminal.png");

	// TODO: copied from GuiMEMonitorable. It looks not changed, maybe unneeded?
	private final int offsetX = 9;

	private final HashMap<Long, ClientDCInternalInv> byId = new HashMap<>();
	private final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
	private final ArrayList<String> names = new ArrayList<>();
	private final ArrayList<Object> lines = new ArrayList<>();
	private GuiImgButtonBooster autoConsumeBoostersBox;
	private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();

	private boolean refreshList = false;
	private GuiMETextField searchField;

	public GuiWIT(final Container container) {
		super(container);

		final GuiScrollbar scrollbar = new GuiScrollbar();
		setScrollBar(scrollbar);
		xSize = 195;
		ySize = 222;
	}

	@Override
	public void initGui() {
		super.initGui();

		getScrollBar().setLeft(175);
		getScrollBar().setHeight(106);
		getScrollBar().setTop(18);

		searchField = new GuiMETextField(fontRenderer, guiLeft + Math.max(104, offsetX), guiTop + 4, 65, 12);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setMaxStringLength(25);
		searchField.setTextColor(0xFFFFFF);
		searchField.setVisible(true);
		searchField.setFocused(true);
		if (!WTApi.instance().getConfig().isOldInfinityMechanicEnabled() && !WTApi.instance().isWTCreative(getWirelessTerminal())) {
			getButtonPanel().addButton(autoConsumeBoostersBox = new GuiImgButtonBooster(getButtonPanelXOffset(), getButtonPanelYOffset(), getContainer().getWirelessTerminal()));
		}
		getButtonPanel().init(this);
	}

	@Override
	public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		fontRenderer.drawString(getGuiDisplayName(GuiText.InterfaceTerminal.getLocal()), 8, 6, 4210752);
		fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752);
		final int ex = getScrollBar().getCurrentScroll();
		final Iterator<Slot> o = inventorySlots.inventorySlots.iterator();
		while (o.hasNext()) {
			if (o.next() instanceof SlotDisconnected) {
				o.remove();
			}
		}
		int offset = 17;
		for (int x = 0; x < LINES_ON_PAGE && ex + x < lines.size(); x++) {
			final Object lineObj = lines.get(ex + x);
			if (lineObj instanceof ClientDCInternalInv) {
				final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;
				for (int z = 0; z < inv.getInventory().getSlots(); z++) {
					inventorySlots.inventorySlots.add(new SlotDisconnected(inv, z, z * 18 + 8, 1 + offset));
				}
			}
			else if (lineObj instanceof String) {
				String name = (String) lineObj;
				final int rows = byName.get(name).size();
				if (rows > 1) {
					name = name + " (" + rows + ')';
				}

				while (name.length() > 2 && fontRenderer.getStringWidth(name) > 155) {
					name = name.substring(0, name.length() - 1);
				}
				fontRenderer.drawString(name, 10, 6 + offset, 4210752);
			}
			offset += 18;
		}
	}

	@Override
	protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
		searchField.mouseClicked(xCoord, yCoord, btn);
		if (btn == 1 && searchField.isMouseIn(xCoord, yCoord)) {
			searchField.setText("");
			refreshList();
		}
		super.mouseClicked(xCoord, yCoord, btn);
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);

		int offset = 17;
		final int ex = getScrollBar().getCurrentScroll();

		for (int x = 0; x < LINES_ON_PAGE && ex + x < lines.size(); x++) {
			final Object lineObj = lines.get(ex + x);
			if (lineObj instanceof ClientDCInternalInv) {
				final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;

				GlStateManager.color(1, 1, 1, 1);
				final int width = inv.getInventory().getSlots() * 18;
				this.drawTexturedModalRect(offsetX + 7, offsetY + offset, 7, 139, width, 18);
			}
			offset += 18;
		}

		if (searchField != null) {
			searchField.drawTextBox();
		}
	}

	@Override
	protected void keyTyped(final char character, final int key) throws IOException {
		if (!checkHotbarKeys(key)) {
			if (character == ' ' && searchField.getText().isEmpty()) {
				return;
			}

			if (searchField.textboxKeyTyped(character, key)) {
				refreshList();
			}
			else {
				super.keyTyped(character, key);
			}
		}
	}

	public void postUpdate(final NBTTagCompound in) {
		if (in.getBoolean("clear")) {
			byId.clear();
			refreshList = true;
		}

		for (final Object oKey : in.getKeySet()) {
			final String key = (String) oKey;
			if (key.startsWith("=")) {
				try {
					final long id = Long.parseLong(key.substring(1), Character.MAX_RADIX);
					final NBTTagCompound invData = in.getCompoundTag(key);
					final ClientDCInternalInv current = getById(id, invData.getLong("sortBy"), invData.getString("un"));

					for (int x = 0; x < current.getInventory().getSlots(); x++) {
						final String which = Integer.toString(x);
						if (invData.hasKey(which)) {
							current.getInventory().setStackInSlot(x, new ItemStack(invData.getCompoundTag(which)));
						}
					}
				}
				catch (final NumberFormatException ignored) {
				}
			}
		}

		if (refreshList) {
			refreshList = false;
			// invalid caches on refresh
			cachedSearches.clear();
			refreshList();
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		final int i = Mouse.getEventDWheel();
		if (i != 0 && getScrollBar() != null) {
			getScrollBar().wheel(i);
		}
	}

	/**
	 * Rebuilds the list of interfaces.
	 *
	 * Respects a search term if present (ignores case) and adding only matching patterns.
	 */
	private void refreshList() {
		byName.clear();

		final String searchFilterLowerCase = searchField.getText().toLowerCase();

		final Set<Object> cachedSearch = getCacheForSearchTerm(searchFilterLowerCase);
		final boolean rebuild = cachedSearch.isEmpty();

		for (final ClientDCInternalInv entry : byId.values()) {
			// ignore inventory if not doing a full rebuild or cache already marks it as miss.
			if (!rebuild && !cachedSearch.contains(entry)) {
				continue;
			}

			// Shortcut to skip any filter if search term is ""/empty
			boolean found = searchFilterLowerCase.isEmpty();

			// Search if the current inventory holds a pattern containing the search term.
			if (!found && !searchFilterLowerCase.isEmpty()) {
				for (final ItemStack itemStack : entry.getInventory()) {
					found = itemStackMatchesSearchTerm(itemStack, searchFilterLowerCase);
					if (found) {
						break;
					}
				}
			}

			// if found, filter skipped or machine name matching the search term, add it
			if (found || entry.getName().toLowerCase().contains(searchFilterLowerCase)) {
				byName.put(entry.getName(), entry);
				cachedSearch.add(entry);
			}
			else {
				cachedSearch.remove(entry);
			}
		}

		names.clear();
		names.addAll(byName.keySet());

		Collections.sort(names);

		lines.clear();
		lines.ensureCapacity(getMaxRows());

		for (final String n : names) {
			lines.add(n);

			final ArrayList<ClientDCInternalInv> clientInventories = new ArrayList<>();
			clientInventories.addAll(byName.get(n));

			Collections.sort(clientInventories);
			lines.addAll(clientInventories);
		}

		getScrollBar().setRange(0, lines.size() - LINES_ON_PAGE, 2);
	}

	private boolean itemStackMatchesSearchTerm(final ItemStack itemStack, final String searchTerm) {
		if (itemStack.isEmpty()) {
			return false;
		}

		final NBTTagCompound encodedValue = itemStack.getTagCompound();

		if (encodedValue == null) {
			return false;
		}

		final NBTTagList outTag = encodedValue.getTagList("out", 10);

		for (int i = 0; i < outTag.tagCount(); i++) {

			final ItemStack parsedItemStack = new ItemStack(outTag.getCompoundTagAt(i));
			if (!parsedItemStack.isEmpty()) {
				final String displayName = Platform.getItemDisplayName(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(parsedItemStack)).toLowerCase();
				if (displayName.contains(searchTerm)) {
					return true;
				}
			}
		}
		return false;
	}

	private Set<Object> getCacheForSearchTerm(final String searchTerm) {
		if (!cachedSearches.containsKey(searchTerm)) {
			cachedSearches.put(searchTerm, new HashSet<>());
		}

		final Set<Object> cache = cachedSearches.get(searchTerm);

		if (cache.isEmpty() && searchTerm.length() > 1) {
			cache.addAll(getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
			return cache;
		}

		return cache;
	}

	private int getMaxRows() {
		return names.size() + byId.size();
	}

	private ClientDCInternalInv getById(final long id, final long sortBy, final String string) {
		ClientDCInternalInv o = byId.get(id);

		if (o == null) {
			byId.put(id, o = new ClientDCInternalInv(9, id, sortBy, string));
			refreshList = true;
		}

		return o;
	}

	@Override
	protected void handleMouseClick(final Slot slot, final int slotIdx, final int mouseButton, final ClickType clickType) {
		final EntityPlayer player = Minecraft.getMinecraft().player;
		if (slot instanceof SlotFake) {
			final InventoryAction action = clickType == ClickType.QUICK_CRAFT ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
			if (drag_click.size() > 1) {
				return;
			}
			final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
			ModNetworking.instance().sendToServer(p);
			return;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (enableSpaceClicking()) {
				IAEItemStack stack = null;
				if (slot instanceof SlotME) {
					stack = ((SlotME) slot).getAEStack();
				}

				int slotNum = getInventorySlots().size();

				if (!(slot instanceof SlotME) && slot != null) {
					slotNum = slot.slotNumber;
				}

				((ContainerWT) inventorySlots).setTargetStack(stack);

				final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, slotNum, 0);
				ModNetworking.instance().sendToServer(p);
				return;
			}
		}

		if (slot instanceof SlotDisconnected) {
			InventoryAction action = null;
			switch (clickType) {
			case PICKUP: // pickup / set-down.
				action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				break;
			case QUICK_MOVE:
				action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				break;

			case CLONE: // creative dupe:

				if (player.capabilities.isCreativeMode) {
					action = InventoryAction.CREATIVE_DUPLICATE;
				}

				break;
			default:
			case THROW: // drop item:
			}

			if (action != null) {
				final PacketInventoryAction p = new PacketInventoryAction(action, slot.getSlotIndex(), ((SlotDisconnected) slot).getSlot().getId());
				ModNetworking.instance().sendToServer(p);
			}
			return;
		}

		if (slot instanceof SlotME) {
			InventoryAction action = null;
			IAEItemStack stack = null;

			switch (clickType) {
			case PICKUP: // pickup / set-down.
				action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				stack = ((SlotME) slot).getAEStack();

				if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && player.inventory.getItemStack().isEmpty() && (stack.getStackSize() == 0 || stack.getStackSize() > 0 && GuiScreen.isAltKeyDown())) {
					action = InventoryAction.AUTO_CRAFT;
				}

				break;
			case QUICK_MOVE:
				action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				stack = ((SlotME) slot).getAEStack();
				break;

			case CLONE: // creative dupe:
				stack = ((SlotME) slot).getAEStack();
				if (stack != null && stack.isCraftable()) {
					action = InventoryAction.AUTO_CRAFT;
				}
				else if (player.capabilities.isCreativeMode) {
					final IAEItemStack slotItem = ((SlotME) slot).getAEStack();
					if (slotItem != null) {
						action = InventoryAction.CREATIVE_DUPLICATE;
					}
				}
				break;

			default:
			case THROW: // drop item:
			}
			if (action != null) {
				if (inventorySlots instanceof ContainerWIT) {
					((ContainerWIT) inventorySlots).setTargetStack(stack);
					final PacketInventoryAction p = new PacketInventoryAction(action, getInventorySlots().size(), 0);
					ModNetworking.instance().sendToServer(p);
				}
			}
			return;
		}

		if (!disableShiftClick && isShiftKeyDown()) {
			disableShiftClick = true;

			if (dbl_whichItem == null || bl_clicked != slot || dbl_clickTimer.elapsed(TimeUnit.MILLISECONDS) > 150) {
				// some simple double click logic.
				bl_clicked = slot;
				dbl_clickTimer = Stopwatch.createStarted();
				if (slot != null) {
					dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
				}
				else {
					dbl_whichItem = ItemStack.EMPTY;
				}
			}
			else if (!dbl_whichItem.isEmpty()) {
				// a replica of the weird broken vanilla feature.

				final List<Slot> slots = getInventorySlots();
				for (final Slot inventorySlot : slots) {
					if (inventorySlot != null && inventorySlot.canTakeStack(Minecraft.getMinecraft().player) && inventorySlot.getHasStack() && inventorySlot.inventory == slot.inventory && Container.canAddItemToSlot(inventorySlot, dbl_whichItem, true)) {
						handleMouseClick(inventorySlot, inventorySlot.slotNumber, 1, clickType);
					}
				}
			}

			disableShiftClick = false;
		}
		super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
	}

	@Override
	protected void actionPerformed(final GuiButton btn) throws IOException {
		if (btn == autoConsumeBoostersBox) {
			autoConsumeBoostersBox.cycleValue();
			return;
		}
		super.actionPerformed(btn);
	}

}
