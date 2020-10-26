package io.sapl.server.ce.service.sapldocument;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.sapl.server.ce.model.sapldocument.SaplDocument;
import io.sapl.server.ce.model.sapldocument.SaplDocumentType;
import io.sapl.server.ce.model.sapldocument.SaplDocumentVersion;
import io.sapl.server.ce.pdp.SaplDocumentPublisher;
import io.sapl.server.ce.persistence.SaplDocumentsRepository;
import io.sapl.server.ce.persistence.SaplDocumentsVersionRepository;
import io.sapl.server.ce.utils.SaplDocumentUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for reading and managing {@link SaplDocument} instances.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaplDocumentService {
	private final SaplDocumentsRepository saplDocumentRepository;
	private final SaplDocumentsVersionRepository saplDocumentVersionRepository;
	private final SaplDocumentPublisher saplDocumentPublisher;

	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
			.withLocale(Locale.GERMANY).withZone(ZoneId.systemDefault());

	/**
	 * Gets all {@link SaplDocument}s.
	 * 
	 * @return the instances
	 */
	public Collection<SaplDocument> getAll() {
		return this.saplDocumentRepository.findAll();
	}

	/**
	 * Gets a specific {@link SaplDocument} by its id.
	 * 
	 * @param id the id of the {@link SaplDocument}
	 * @return the {@link SaplDocument}
	 */
	public SaplDocument getById(long id) {
		Optional<SaplDocument> optionalEntity = this.saplDocumentRepository.findById(id);
		if (!optionalEntity.isPresent()) {
			throw new IllegalArgumentException(String.format("entity with id %d is not existing", id));
		}

		return optionalEntity.get();
	}

	/**
	 * Gets the amount of {@link SaplDocument}s.
	 * 
	 * @return the amount
	 */
	public long getAmount() {
		return this.saplDocumentRepository.count();
	}

	/**
	 * Creates a new {@link SaplDocument}.
	 * 
	 * @param documentValue the document value of the initial version
	 * 
	 * @return the created {@link SaplDocument}
	 */
	public SaplDocument create(String documentValue) {
		SaplDocumentType type = SaplDocumentUtils.getType(documentValue);
		String name = SaplDocumentUtils.getNameFromDocumentValue(documentValue);

		SaplDocument saplDocumentToCreate = new SaplDocument().setLastModified(this.getCurrentTimestampAsString())
				.setName(name).setCurrentVersionNumber(1).setType(type);
		SaplDocument createdDocument = this.saplDocumentRepository.save(saplDocumentToCreate);

		SaplDocumentVersion initialSaplDocumentVersion = new SaplDocumentVersion().setSaplDocument(createdDocument)
				.setVersionNumber(1).setValue(documentValue).setName(name);
		this.saplDocumentVersionRepository.save(initialSaplDocumentVersion);

		return createdDocument;
	}

	/**
	 * Creates a new version of a {@link SaplDocument}.
	 * 
	 * @param saplDocumentId the id of the {@link SaplDocument} in which the version
	 *                       will be added
	 * @param documentValue  the document value of the version
	 * @return the created {@link SaplDocumentVersion}
	 */
	public SaplDocumentVersion createVersion(long saplDocumentId, @NonNull String documentValue) {
		SaplDocument saplDocument = this.getById(saplDocumentId);

		int newVersionNumber = saplDocument.getCurrentVersionNumber() + 1;
		SaplDocumentType type = SaplDocumentUtils.getType(documentValue);
		String newName = SaplDocumentUtils.getNameFromDocumentValue(documentValue);

		SaplDocumentVersion newSaplDocumentVersion = new SaplDocumentVersion().setSaplDocument(saplDocument)
				.setVersionNumber(newVersionNumber).setValue(documentValue).setName(newName);
		this.saplDocumentVersionRepository.save(newSaplDocumentVersion);

		saplDocument.setCurrentVersionNumber(newVersionNumber).setLastModified(this.getCurrentTimestampAsString())
				.setType(type).setName(newName);
		this.saplDocumentRepository.save(saplDocument);

		return newSaplDocumentVersion;
	}

	/**
	 * Publishes a specific version of a {@link SaplDocument}.
	 * 
	 * @param saplDocumentId   the id of the {@link SaplDocument}
	 * @param versionToPublish the version to publish
	 * @throws PublishedDocumentNameCollisionException thrown if the name of a
	 *                                                       {@link SaplDocument} to
	 *                                                       publish is not unique
	 * 
	 */
	public void publishVersion(long saplDocumentId, int versionToPublish)
			throws PublishedDocumentNameCollisionException {
		SaplDocument saplDocument = this.getById(saplDocumentId);
		SaplDocumentVersion saplDocumentVersionToPublish = saplDocument.getVersion(versionToPublish);

		this.checkForUniqueNameOfSaplDocumentToPublish(saplDocumentVersionToPublish);

		// update document
		saplDocument.setPublishedVersion(saplDocumentVersionToPublish)
				.setLastModified(this.getCurrentTimestampAsString());
		this.saplDocumentRepository.save(saplDocument);

		log.info(String.format("publish SAPL document: %s", saplDocumentVersionToPublish.getName()));

		this.saplDocumentPublisher.publishSaplDocument(saplDocument);
	}

	/**
	 * Unpublishes the published version of a {@link SaplDocument}.
	 * 
	 * @param saplDocumentId the id of the {@link SaplDocument}
	 */
	public void unpublishVersion(long saplDocumentId) {
		SaplDocument saplDocumentToUnpublish = this.getById(saplDocumentId);

		saplDocumentToUnpublish.setPublishedVersion(null);
		this.saplDocumentRepository.save(saplDocumentToUnpublish);

		log.info(String.format("unpublish SAPL document: %s", saplDocumentToUnpublish.getName()));

		this.saplDocumentPublisher.unpublishSaplDocument(saplDocumentToUnpublish);
	}

	private String getCurrentTimestampAsString() {
		return this.dateFormatter.format(Instant.now());
	}

	private void checkForUniqueNameOfSaplDocumentToPublish(@NonNull SaplDocumentVersion saplDocumentVersionToPublish)
			throws PublishedDocumentNameCollisionException {
		String name = SaplDocumentUtils.getNameFromDocumentValue(saplDocumentVersionToPublish.getValue());

		Collection<SaplDocument> saplDocumentsWithPublishedVersionWithEqualName = this.saplDocumentRepository
				.getSaplDocumentsByNameOfPublishedVersion(name);

		// throw appropriate exception for first conflicting SAPL document
		for (SaplDocument saplDocument : saplDocumentsWithPublishedVersionWithEqualName) {
			throw new PublishedDocumentNameCollisionException(saplDocument.getId(),
					saplDocument.getPublishedVersion().getVersionNumber());
		}
	}
}