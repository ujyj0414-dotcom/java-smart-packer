package com.smartpacker.util;

import com.smartpacker.domain.item.Item;
import java.util.ArrayList;
import java.util.List;

public class FileImportResult {
    public final List<Item> successItems = new ArrayList<>();
    public final List<String> failedLines = new ArrayList<>();
}