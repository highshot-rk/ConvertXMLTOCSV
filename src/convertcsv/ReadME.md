-- Requirements
1. GUI program. User can select zip file to process. X
2. Show file names that is processing current. X
3. In processing EDRM.XML file, how many document is written to CSV file.
4. Automatically unpack zip file. X
5. Find the EDRM.XML file. There is not specific location. X
6. Write a row per each document. X
7. Write child node attributes of document as column in CSV file. And in <Document> tag. attributes are header, attribute values are value of column X
8. Handle multi-line values and quotes in text. X
9. <root><Batch><Documents><Document /><Document /></Documents></Batch></root> X
    This is document tree structure. X
10. <Batch> node is not direct under of <root> node. X
11. Save to same directory and same name of zip file. X
12. All values can be used as String. X

** column name must be attribute name, not fixed. X


******** During Saving Data as CSV file, I can use File manage way, but i will use CSVWriter.
