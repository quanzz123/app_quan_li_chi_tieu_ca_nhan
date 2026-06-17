package com.example.app_quan_li_chi_tieu_ca_nhan.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TFLiteClassifier {

    private final Interpreter interpreter;
    private final List<String> labels;
    private static final int INPUT_SIZE = 224;

    public TFLiteClassifier(Context context) throws IOException {
        // Nạp mô hình TFLite và danh sách nhãn từ assets
        interpreter = new Interpreter(loadModelFile(context, "model.tflite"));
        labels = loadLabelList(context, "labels.txt");
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(Context context, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(labelPath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    labelList.add(line.trim());
                }
            }
        }
        return labelList;
    }

    public String classifyImage(Bitmap bitmap) {
        if (interpreter == null || labels.isEmpty()) {
            return "Khác";
        }

        // 1. Resize ảnh Bitmap về kích thước 224x224
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        // 2. Chuẩn bị ByteBuffer cho đầu vào: 1 * 224 * 224 * 3 (kênh RGB) * 4 (float: 4 bytes)
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        // 3. Chuẩn hóa pixel ảnh và ghi vào ByteBuffer (MobileNetV2 scale về [-1, 1])
        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                // Tách kênh màu R, G, B và đưa về dải [-1.0f, 1.0f]
                inputBuffer.putFloat((((val >> 16) & 0xFF) - 127.5f) / 127.5f);
                inputBuffer.putFloat((((val >> 8) & 0xFF) - 127.5f) / 127.5f);
                inputBuffer.putFloat(((val & 0xFF) - 127.5f) / 127.5f);
            }
        }

        // 4. Chuẩn bị mảng đầu ra: 1 dòng * 3 lớp (drinks, food, shopping)
        float[][] output = new float[1][labels.size()];

        // 5. Chạy suy luận
        interpreter.run(inputBuffer, output);

        // 6. Tìm index có xác suất lớn nhất
        int maxIdx = 0;
        float maxProb = output[0][0];
        for (int i = 1; i < labels.size(); i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIdx = i;
            }
        }

        // 7. Lấy tên nhãn và định dạng lại chữ cái đầu viết hoa (ví dụ: drinks -> Drinks)
        String category = labels.get(maxIdx);
        if (category != null && !category.isEmpty()) {
            category = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
        } else {
            category = "Khác";
        }

        return category;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
}
