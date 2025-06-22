const mongoose = require('mongoose');

const InventoryItemSchema = new mongoose.Schema({
  itemName: { type: String, required: true },
  itemQty: { type: Number, required: true },
  itemNotifs: { type: Boolean, default: false }
}, {
  collection: 'Inventory'
});

InventoryItemSchema.virtual('itemId').get(function () {
  return this._id.toHexString();
});
InventoryItemSchema.set('toJSON', {
  virtuals: true
});

module.exports = mongoose.model('InventoryItem', InventoryItemSchema);
