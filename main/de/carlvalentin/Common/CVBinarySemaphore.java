package de.carlvalentin.Common;

/**
 * Binaere Semaphore
 */
public class CVBinarySemaphore 
{
	/**
	 * Wenn true, Semaphore belegt (grabbed)
	 */
	private boolean lk_bGrabbed;
	
	/**
	 * Konstruktor der Klasse CVBinarySemaphore
	 * 
	 */
	public CVBinarySemaphore()
	{
		this.lk_bGrabbed = false;
		
		return;
	}
	
	/**
	 * Semaphore anfordern
	 *
	 */
	public synchronized void grab()
	{
		while(this.lk_bGrabbed == true)
		{
			try
			{
				wait();
			}
			catch(InterruptedException ex)
			{
				
			}
		}
		
		this.lk_bGrabbed = true;
		
		return;
	}
	
	/**
	 * Semaphore freigeben
	 *
	 */
	public synchronized void release()
	{
		this.lk_bGrabbed = false;
		
		notify();
		
		return;
	}
}
