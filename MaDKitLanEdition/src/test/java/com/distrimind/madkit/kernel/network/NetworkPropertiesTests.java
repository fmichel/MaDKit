package com.distrimind.madkit.kernel.network;

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.kernel.KernelAddress;

public class NetworkPropertiesTests {
	@Test
	public void testAcceptedSerializedClasses()
	{
		NetworkProperties np=new NetworkProperties();
		
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("org.apache.commons.collections.functors.InvokerTransformer"));
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("org.apache.commons.collections.functors.InstantiateTransformer"));
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("org.apache.commons.collections4.functors.InvokerTransformer"));
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("org.apache.commons.collections4.functors.InstantiateTransformer"));
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("org.codehaus.groovy.runtime.ConvertedClosure"));
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("org.codehaus.groovy.runtime.MethodClosure"));
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("org.springframework.beans.factory.ObjectFactory"));
		Assert.assertTrue(np.isDeniedClassForSerializationUsingPatterns("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"));
		Assert.assertFalse(np.isDeniedClassForSerializationUsingPatterns("com.distrimind.madkit.kernel.Madkit"));
		
		Assert.assertFalse(np.isDeniedClassForSerializationUsingBlackClassList(KernelAddress.class));

		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("org.apache.commons.collections.functors.InvokerTransformer"));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("org.apache.commons.collections.functors.InstantiateTransformer"));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("org.apache.commons.collections4.functors.InvokerTransformer"));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("org.apache.commons.collections4.functors.InstantiateTransformer"));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("org.codehaus.groovy.runtime.ConvertedClosure"));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("org.codehaus.groovy.runtime.MethodClosure"));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("org.springframework.beans.factory.ObjectFactory"));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingPatterns("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"));
		Assert.assertTrue(np.isAcceptedClassForSerializationUsingPatterns("com.distrimind.madkit.kernel.Madkit"));
		
		Assert.assertTrue(np.isAcceptedClassForSerializationUsingWhiteClassList(Long.class));
		Assert.assertTrue(np.isAcceptedClassForSerializationUsingWhiteClassList(KernelAddress.class));
		Assert.assertFalse(np.isAcceptedClassForSerializationUsingWhiteClassList(NetworkPropertiesTests.class));
	
	}

}
