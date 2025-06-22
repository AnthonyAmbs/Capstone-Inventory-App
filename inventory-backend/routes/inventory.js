const express = require('express');
const router = express.Router();
const InventoryItem = require('../models/InventoryItem');

// Get all inventory items
router.get('/', async (req, res) => {
  try {
    const items = await InventoryItem.find();
    res.json(items);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Server error' });
  }
});

// Add a new item
router.post('/', async (req, res) => {
  try {
    const { itemName, itemQty, itemNotifs } = req.body;
    const newItem = new InventoryItem({ itemName, itemQty, itemNotifs });
    const savedItem = await newItem.save();
    res.json(savedItem);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Server error' });
  }
});

// Update an existing item by ID
router.put('/:itemId', async (req, res) => {
  try {
    const { itemId } = req.params;
    const { itemName, itemQty, itemNotifs } = req.body;

    const updatedItem = await InventoryItem.findByIdAndUpdate(
      itemId,
      { itemName, itemQty, itemNotifs },
      { new: true }
    );

    if (!updatedItem) {
      return res.status(404).json({ message: 'Item not found' });
    }

    res.json(updatedItem);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Server error' });
  }
});

// Delete an item by ID
router.delete('/:itemId', async (req, res) => {
  try {
    const { itemId } = req.params;

    const deleted = await InventoryItem.findByIdAndDelete(itemId);
    if (!deleted) {
      return res.status(404).json({ message: 'Item not found' });
    }

    res.json({ message: 'Item deleted' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Server error' });
  }
});

// Health check endpoint
router.get('/health-check', (req, res) => {
  res.send('ping');
});

module.exports = router;
