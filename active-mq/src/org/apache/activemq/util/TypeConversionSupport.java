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
 * @Description: �����˶������ת���������ṩ��һЩ������ɶ�������ת���ķ�����ʹ����Щ�������Խ�����ת��Ϊ��Ҫ������
 * @author: ����
 * @date: 2016��7��1�� ����9:43:58
 */
public final class TypeConversionSupport {
	/**
	 * �������ת����ʲô������������ֻ�Ǽ򵥵Ľ���η���
	 */
	private static final Converter IDENTITY_CONVERTER = new Converter() {
		@Override
		public Object convert(Object value) {
			return value;
		}
	};

	/**
	 * @ClassName: ConversionKey
	 * @Description: ��CONVERSION_MAP�����д洢�˶��ConversionKey������ת�����ԣ�����ʹ��ConversionKey�ҵ���Ӧ������ת����
	 *               CONVERSION_MAP��һ��Map���͵ģ��������������д��equals��hashCode������
	 * @author: ����
	 * @date: 2016��7��1�� ����9:21:49
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
	 * @Description: ����ת��������һ��interface������ֻ��һ��convert����
	 * @author: ����
	 * @date: 2016��7��1�� ����9:26:32
	 */
	public interface Converter {
		/**
		 * @Title: convert
		 * @Description: ��ɶ��������ת������
		 * @param value
		 * @return
		 * @return: Object
		 */
		Object convert(Object value);
	}

	/**
	 * CONVERSION_MAP�д洢���ַ������װ�ࡢ��װ�����װ��֮���ת����������֮�⣬�����ַ���ת��ΪActiveMQDestination�Լ��ַ���ת��ΪURI
	 */
	private static final Map<ConversionKey, Converter> CONVERSION_MAP = new HashMap<ConversionKey, Converter>();
	static {
		/**
		 * ���ڽ����ת��Ϊ�ַ���
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
	 * @Description: ����һ����ζ���ת��Ϊ�ڶ������Ҫ������ͣ�ϵͳ�ڲ������˶������ת�������������һЩ���������ת������������������ת�����ͷ���null
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
	 * @Description:����������������ҵ���Ӧ������ת����������Ҳ������ͷ���null
	 * @param from
	 * @param to
	 * @return
	 * @return: Converter
	 */
	public static Converter lookupConverter(Class<?> from, Class<?> to) {
		// use wrapped type for primitives
		// isPrimitive�����ж��Ƿ��ǻ������ͣ�����������8�л������ͺ�void����
		if (from.isPrimitive()) {
			// ����ķ�������������ת��Ϊ���Ӧ�İ�װ�࣬����û�ж�char�ͽ���ת��
			from = convertPrimitiveTypeToWrapperType(from);
		}
		if (to.isPrimitive()) {
			to = convertPrimitiveTypeToWrapperType(to);
		}
		if (from.equals(to)) {
			// ��������������ͬ�Ķ�����ô�����������ֵ��һ������ת�������������ת���������κβ�����ֻ�Ǽ򵥵Ľ���η���
			return IDENTITY_CONVERTER;
		}
		// ����from��to�������ҵ���Ӧ������ת���������û�оͷ���null
		return CONVERSION_MAP.get(new ConversionKey(from, to));
	}
	/**
	 * Converts primitive types such as int to its wrapper type like {@link Integer}
	 */
	// ���type��8�ֳ��õĻ������ͣ���ת��Ϊ���װ�࣬������Ǿ�ֱ�ӷ��ء�
	// �����е���������char��
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
