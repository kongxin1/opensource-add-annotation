/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.util;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.command.ActiveMQDestination;
import org.fusesource.hawtbuf.UTF8Buffer;

/**
 * Type conversion support for ActiveMQ.
 */
/**
 * @ClassName: TypeConversionSupport
 * @Description: 包含了多个类型转化器，并提供了一些用于完成对象类型转化的方法，使用这些方法可以将对象转化为需要的类型
 * @author: 孔新
 * @date: 2016年7月1日 下午9:43:58
 */
public final class TypeConversionSupport {
	/**
	 * 下面这个转换类什么工作都不做，只是简单的将入参返回
	 */
	private static final Converter IDENTITY_CONVERTER = new Converter() {
		@Override
		public Object convert(Object value) {
			return value;
		}
	};

	/**
	 * @ClassName: ConversionKey
	 * @Description: 在CONVERSION_MAP属性中存储了多个ConversionKey与类型转化器对，可以使用ConversionKey找到对应的类型转换器
	 *               CONVERSION_MAP是一个Map类型的，所以下面的类重写了equals和hashCode方法。
	 * @author: 孔新
	 * @date: 2016年7月1日 下午9:21:49
	 */
	private static class ConversionKey {
		final Class<?> from;
		final Class<?> to;
		final int hashCode;

		public ConversionKey(Class<?> from, Class<?> to) {
			this.from = from;
			this.to = to;
			this.hashCode = from.hashCode() ^ (to.hashCode() << 1);
		}
		@Override
		public boolean equals(Object o) {
			ConversionKey x = (ConversionKey) o;
			return x.from == from && x.to == to;
		}
		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	/**
	 * @ClassName: Converter
	 * @Description: 类型转化器，是一个interface，里面只有一个convert方法
	 * @author: 孔新
	 * @date: 2016年7月1日 下午9:26:32
	 */
	public interface Converter {
		/**
		 * @Title: convert
		 * @Description: 完成对象的类型转化工作
		 * @param value
		 * @return
		 * @return: Object
		 */
		Object convert(Object value);
	}

	/**
	 * CONVERSION_MAP中存储了字符串与包装类、包装类与包装类之间的转换器，除此之外，还有字符串转化为ActiveMQDestination以及字符串转化为URI
	 */
	private static final Map<ConversionKey, Converter> CONVERSION_MAP = new HashMap<ConversionKey, Converter>();
	static {
		/**
		 * 用于将入参转化为字符串
		 */
		Converter toStringConverter = new Converter() {
			@Override
			public Object convert(Object value) {
				return value.toString();
			}
		};
		CONVERSION_MAP.put(new ConversionKey(Boolean.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(Byte.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(Short.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(Integer.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(Long.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(Float.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(Double.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(UTF8Buffer.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(URI.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(BigInteger.class, String.class), toStringConverter);
		CONVERSION_MAP.put(new ConversionKey(String.class, Boolean.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Boolean.valueOf((String) value);
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, Byte.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Byte.valueOf((String) value);
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, Short.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Short.valueOf((String) value);
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, Integer.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Integer.valueOf((String) value);
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, Long.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Long.valueOf((String) value);
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, Float.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Float.valueOf((String) value);
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, Double.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Double.valueOf((String) value);
			}
		});
		Converter longConverter = new Converter() {
			@Override
			public Object convert(Object value) {
				return Long.valueOf(((Number) value).longValue());
			}
		};
		CONVERSION_MAP.put(new ConversionKey(Byte.class, Long.class), longConverter);
		CONVERSION_MAP.put(new ConversionKey(Short.class, Long.class), longConverter);
		CONVERSION_MAP.put(new ConversionKey(Integer.class, Long.class), longConverter);
		CONVERSION_MAP.put(new ConversionKey(Date.class, Long.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Long.valueOf(((Date) value).getTime());
			}
		});
		Converter intConverter = new Converter() {
			@Override
			public Object convert(Object value) {
				return Integer.valueOf(((Number) value).intValue());
			}
		};
		CONVERSION_MAP.put(new ConversionKey(Byte.class, Integer.class), intConverter);
		CONVERSION_MAP.put(new ConversionKey(Short.class, Integer.class), intConverter);
		CONVERSION_MAP.put(new ConversionKey(Byte.class, Short.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return Short.valueOf(((Number) value).shortValue());
			}
		});
		CONVERSION_MAP.put(new ConversionKey(Float.class, Double.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return new Double(((Number) value).doubleValue());
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, ActiveMQDestination.class), new Converter() {
			@Override
			public Object convert(Object value) {
				return ActiveMQDestination.createDestination((String) value, ActiveMQDestination.QUEUE_TYPE);
			}
		});
		CONVERSION_MAP.put(new ConversionKey(String.class, URI.class), new Converter() {
			@Override
			public Object convert(Object value) {
				String text = value.toString();
				try {
					return new URI(text);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private TypeConversionSupport() {
	}
	/**
	 * @Title: convert
	 * @Description: 将第一个入参对象转化为第二个入参要求的类型，系统内部创建了多个类型转化器，可以完成一些对象的类型转化工作，如果不能完成转化，就返回null
	 * @param value
	 * @param to
	 * @return
	 * @return: Object
	 */
	public static Object convert(Object value, Class<?> to) {
		if (value == null) {
			// lets avoid NullPointerException when converting to boolean for null values
			if (boolean.class.isAssignableFrom(to)) {
				return Boolean.FALSE;
			}
			return null;
		}
		// eager same instance type test to avoid the overhead of invoking the type converter
		// if already same type
		if (to.isInstance(value)) {
			return to.cast(value);
		}
		// lookup converter
		Converter c = lookupConverter(value.getClass(), to);
		if (c != null) {
			return c.convert(value);
		} else {
			return null;
		}
	}
	/**
	 * @Title: lookupConverter
	 * @Description:根据两个入参类型找到对应的类型转换器，如果找不到，就返回null
	 * @param from
	 * @param to
	 * @return
	 * @return: Converter
	 */
	public static Converter lookupConverter(Class<?> from, Class<?> to) {
		// use wrapped type for primitives
		// isPrimitive用于判断是否是基本类型，基本类型有8中基本类型和void类型
		if (from.isPrimitive()) {
			// 下面的方法将基本类型转化为其对应的包装类，但是没有对char型进行转化
			from = convertPrimitiveTypeToWrapperType(from);
		}
		if (to.isPrimitive()) {
			to = convertPrimitiveTypeToWrapperType(to);
		}
		if (from.equals(to)) {
			// 如果两个入参是相同的对象，那么下面这个返回值是一个对象转换器，这个对象转换器不做任何操作，只是简单的将入参返回
			return IDENTITY_CONVERTER;
		}
		// 根据from和to的类型找到对应的类型转换器，如果没有就返回null
		return CONVERSION_MAP.get(new ConversionKey(from, to));
	}
	/**
	 * Converts primitive types such as int to its wrapper type like {@link Integer}
	 */
	// 如果type是8种常用的基本类型，就转化为其包装类，如果不是就直接返回。
	// 下面列的类型少了char型
	private static Class<?> convertPrimitiveTypeToWrapperType(Class<?> type) {
		Class<?> rc = type;
		if (type.isPrimitive()) {
			if (type == int.class) {
				rc = Integer.class;
			} else if (type == long.class) {
				rc = Long.class;
			} else if (type == double.class) {
				rc = Double.class;
			} else if (type == float.class) {
				rc = Float.class;
			} else if (type == short.class) {
				rc = Short.class;
			} else if (type == byte.class) {
				rc = Byte.class;
			} else if (type == boolean.class) {
				rc = Boolean.class;
			}
		}
		return rc;
	}
}
