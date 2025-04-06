import numpy
import pandas as pd
import sklearn

#采用pandas库的DataFrame格式构造数据集
data = pd.DataFrame({
    '学号':[1,2,3,4,5,6,7,8],
    '身高':[172.0,167.0,169.0,171.0,170.0,168.0,165.0,163.0],
    '体重':[70,62,65,67,66,64,61,59]
})

data.duplicated()