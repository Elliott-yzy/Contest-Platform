const ExcelJS = {
  Workbook: jest.fn().mockImplementation(() => ({
    addWorksheet: jest.fn(() => ({
      addRow: jest.fn(),
    })),
    xlsx: {
      writeBuffer: jest.fn(() => Promise.resolve(new ArrayBuffer(8))),
    },
  })),
};

export default ExcelJS;
